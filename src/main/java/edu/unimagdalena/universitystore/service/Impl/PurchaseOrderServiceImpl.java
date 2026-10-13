package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.event.OrderStatusChangedEvent;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.mapper.OrderMapper;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.repository.OrderStatusHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import edu.unimagdalena.universitystore.service.ShippingEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper mapper;
    private final ShippingEstimator shippingEstimator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        if (req.customerId() == null || !customerRepository.existsByIdNotDeleted(req.customerId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        if (req.addressId() == null || !addressRepository.existsByIdNotDeleted(req.addressId())) {
            throw new ResourceNotFoundException("Address not found");
        }
        if (req.items() == null || req.items().isEmpty()) {
            throw new ValidationException("Order items are required");
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Address address = addressRepository.findById(req.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!shippingEstimator.deliversTo(address)) {
            throw new ValidationException("Orders can only be shipped to addresses in Colombia");
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .status(OrderStatus.CREATED)
                .statusChangedOn(LocalDate.now())
                .customer(customer)
                .address(address)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : req.items()) {
            var product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (itemReq.quantity() == null || itemReq.quantity() <= 0) {
                throw new ValidationException("Quantity must be greater than zero");
            }

            Inventory inventory = inventoryRepository.findByProductIdForUpdate(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

            if (inventory.getAvailableStock() < itemReq.quantity()) {
                throw new ValidationException("Not enough stock for product: " + product.getName());
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - itemReq.quantity());
            inventoryRepository.save(inventory);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            item.calculateSubtotal();

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotal(total);
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(saved)
                .status(OrderStatus.CREATED)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse payOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.CREATED, OrderStatus.PAID);
    }

    @Override
    @Transactional
    public OrderResponse shipOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.PAID, OrderStatus.SHIPPED);
    }

    @Override
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        return transitionStatus(orderId, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED) {
            throw new BusinessException("Order is already " + order.getStatus());
        }

        // Once paid there is money involved: undoing the order goes through the return process
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Paid, shipped or delivered orders cannot be cancelled. Use return process instead.");
        }

        restoreStock(order);

        return transitionStatusInternal(order, OrderStatus.CANCELLED, null);
    }

    @Override
    @Transactional
    public OrderResponse returnOrder(Long orderId, String reason) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Paid but not shipped: the product never left, so it is a plain refund
        if (order.getStatus() == OrderStatus.PAID) {
            restoreStock(order);
            return transitionStatusInternal(order, OrderStatus.RETURNED, reason);
        }

        // In the customer's hands: stock and refund wait until the product is back (confirmReturn)
        if (order.getStatus() == OrderStatus.DELIVERED) {
            return transitionStatusInternal(order, OrderStatus.RETURN_REQUESTED, reason);
        }

        // On its way: nothing to cancel or send back until it arrives
        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new BusinessException("Shipped orders cannot be returned until they are delivered");
        }

        if (order.getStatus() == OrderStatus.RETURN_REQUESTED) {
            throw new BusinessException("A return has already been requested for this order");
        }

        throw new BusinessException("Only paid or delivered orders can be returned");
    }

    @Override
    @Transactional
    public OrderResponse confirmReturn(Long orderId) {
        PurchaseOrder order = findReturnRequested(orderId);

        restoreStock(order);
        return transitionStatusInternal(order, OrderStatus.RETURNED, null);
    }

    @Override
    @Transactional
    public OrderResponse rejectReturn(Long orderId, String reason) {
        PurchaseOrder order = findReturnRequested(orderId);

        // Returns can only be requested on delivered orders, so that is where a rejected one goes back
        return transitionStatusInternal(order, OrderStatus.DELIVERED, reason);
    }

    @Override
    @Transactional
    public OrderResponse changeAddress(Long orderId, Long addressId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("The delivery address can only be changed before the order is shipped");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getCustomer().getId().equals(order.getCustomer().getId())) {
            throw new ValidationException("The address does not belong to the customer of the order");
        }
        if (!shippingEstimator.deliversTo(address)) {
            throw new ValidationException("Orders can only be shipped to addresses in Colombia");
        }

        order.setAddress(address);
        refreshEstimate(order);

        return mapper.toResponse(purchaseOrderRepository.save(order));
    }

    // An edited address may now be in another city: paid orders waiting to ship get a new promise
    @Override
    @Transactional
    public void refreshEstimatedDelivery(Long addressId) {
        for (PurchaseOrder order : purchaseOrderRepository.findByAddressIdAndStatus(addressId, OrderStatus.PAID)) {
            refreshEstimate(order);
            purchaseOrderRepository.save(order);
        }
    }

    private void refreshEstimate(PurchaseOrder order) {
        if (order.getStatus() == OrderStatus.PAID && order.getShipDate() != null) {
            order.setEstimatedDelivery(shippingEstimator.estimatedDelivery(order.getShipDate(), order.getAddress()));
        }
    }

    private PurchaseOrder findReturnRequested(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new BusinessException("Order has no pending return request");
        }
        return order;
    }

    private void restoreStock(PurchaseOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProductIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
            inventory.setAvailableStock(inventory.getAvailableStock() + item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    private OrderResponse transitionStatus(Long orderId, OrderStatus from, OrderStatus to) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != from) {
            throw new BusinessException("Order must be in " + from + " status to transition to " + to);
        }

        return transitionStatusInternal(order, to, null);
    }

    private OrderResponse transitionStatusInternal(PurchaseOrder order, OrderStatus newStatus, String reason) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setStatusChangedOn(LocalDate.now());

        // The estimate is the promise made at payment; the real arrival is settled when the order ships
        if (newStatus == OrderStatus.PAID) {
            order.setShipDate(shippingEstimator.shipDate(LocalDateTime.now()));
            order.setEstimatedDelivery(shippingEstimator.estimatedDelivery(order.getShipDate(), order.getAddress()));
        } else if (newStatus == OrderStatus.SHIPPED) {
            order.setArrivalDate(shippingEstimator.actualArrival(LocalDate.now(), order.getAddress()));
            // The package is on its way to this address: later edits must not change where it went
            if (order.getAddress() != null) {
                order.setShippingAddress(order.getAddress().toLine());
            }
        } else if (newStatus == OrderStatus.DELIVERED && oldStatus == OrderStatus.SHIPPED) {
            order.setDeliveredOn(LocalDate.now());
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(newStatus)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
        orderStatusHistoryRepository.save(history);

        PurchaseOrder saved = purchaseOrderRepository.save(order);

        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, saved, oldStatus, newStatus));

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> search(Long customerId, OrderStatus status) {
        return purchaseOrderRepository.searchOrders(customerId, status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistory(Long orderId) {
        return orderStatusHistoryRepository.findByOrderId(orderId)
                .stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public void softDelete(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.softDelete();
        purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyIncomeResponse> getMonthlyIncome() {
        return purchaseOrderRepository.findMonthlyIncome().stream()
                .map(row -> new MonthlyIncomeResponse(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        (BigDecimal) row[2]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BestSellingProductResponse> getBestSellingProducts() {
        return purchaseOrderRepository.findBestSellingProducts().stream()
                .map(row -> new BestSellingProductResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopCustomerResponse> getTopCustomers() {
        return purchaseOrderRepository.findTopCustomers().stream()
                .map(row -> new TopCustomerResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeByStatusResponse> getIncomeByStatus() {
        return purchaseOrderRepository.findIncomeByStatus().stream()
                .map(row -> new IncomeByStatusResponse(
                        row[0].toString(),
                        (BigDecimal) row[1]
                ))
                .collect(Collectors.toList());
    }
}
