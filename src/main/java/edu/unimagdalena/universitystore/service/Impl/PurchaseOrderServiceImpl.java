package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.repository.OrderStatusHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    @Transactional
    public PurchaseOrder create(PurchaseOrder order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        if (order.getCustomer() == null || order.getCustomer().getId() == null
                || !customerRepository.existsById(order.getCustomer().getId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        if (order.getAddress() == null || order.getAddress().getId() == null
                || !addressRepository.existsById(order.getAddress().getId())) {
            throw new ResourceNotFoundException("Address not found");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException("Order items are required");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new ValidationException("Product is required for each item");
            }

            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ValidationException("Quantity must be greater than zero");
            }

            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

            if (inventory.getAvailableStock() < item.getQuantity()) {
                throw new ValidationException("Not enough stock for product: " + product.getName());
            }

            inventory.setAvailableStock(inventory.getAvailableStock() - item.getQuantity());
            inventoryRepository.save(inventory);

            item.setOrder(order);
            item.setProduct(product);
            item.setUnitPrice(product.getPrice());

            var subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);

            total = total.add(subtotal);
        }

        order.setTotal(total);

        PurchaseOrder saved = purchaseOrderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(saved)
                .status(OrderStatus.CREATED)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        for (OrderItem item : order.getItems()) {
            orderItemRepository.save(item);
        }

        return saved;
    }

    @Override
    @Transactional
    public PurchaseOrder payOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED orders can be paid");
        }

        order.setStatus(OrderStatus.PAID);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PAID)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        return purchaseOrderRepository.save(order);
    }

    @Override
    @Transactional
    public PurchaseOrder cancelOrder(Long orderId) {
        PurchaseOrder order = findById(orderId);

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Paid orders cannot be cancelled");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
            inventory.setAvailableStock(inventory.getAvailableStock() + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setStatus(OrderStatus.CANCELLED);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CANCELLED)
                .changedAt(LocalDateTime.now())
                .build();
        orderStatusHistoryRepository.save(history);

        return purchaseOrderRepository.save(order);
    }

    @Override
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    public PurchaseOrder findById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setItems(orderItemRepository.findByOrderId(order.getId()));
        return order;
    }

    @Override
    public List<PurchaseOrder> search(Long customerId, OrderStatus status) {
        return purchaseOrderRepository.searchOrders(customerId, status);
    }
}
