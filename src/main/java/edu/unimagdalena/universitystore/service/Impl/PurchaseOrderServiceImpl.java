package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.entity.ProductPriceHistory;
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
import edu.unimagdalena.universitystore.repository.ProductPriceHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        if (req.customerId() == null || !customerRepository.existsById(req.customerId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        if (req.addressId() == null || !addressRepository.existsById(req.addressId())) {
            throw new ResourceNotFoundException("Address not found");
        }
        if (req.items() == null || req.items().isEmpty()) {
            throw new ValidationException("Order items are required");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setStatus(OrderStatus.CREATED);
        order.setCustomer(new Customer());
        order.getCustomer().setId(req.customerId());
        order.setAddress(new Address());
        order.getAddress().setId(req.addressId());

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
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())))
                    .build();

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        order.setTotal(total);
        PurchaseOrder saved = purchaseOrderRepository.save(order);

        orderItemRepository.saveAll(saved.getItems());

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

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Shipped or delivered orders cannot be cancelled. Use return process instead.");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            restoreStock(order);
        }

        return transitionStatusInternal(order, OrderStatus.CANCELLED, null);
    }

    @Override
    @Transactional
    public OrderResponse returnOrder(Long orderId, String reason) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Only shipped or delivered orders can be returned");
        }

        restoreStock(order);
        return transitionStatusInternal(order, OrderStatus.RETURNED, reason);
    }

    private void restoreStock(PurchaseOrder order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
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
}