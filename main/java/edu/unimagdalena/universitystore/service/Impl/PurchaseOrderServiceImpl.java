package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.InventoryRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public PurchaseOrder create(PurchaseOrder order) {
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);

        if (order.getTotal() == null) {
            throw new ValidationException("Total is required");
        }

        return purchaseOrderRepository.save(order);
    }

    @Override
    public PurchaseOrder payOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED orders can be paid");
        }

        order.setStatus(OrderStatus.PAID);

        return purchaseOrderRepository.save(order);
    }

    @Override
    public PurchaseOrder cancelOrder(Long orderId) {
        PurchaseOrder order = findById(orderId);

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Paid orders cannot be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        return purchaseOrderRepository.save(order);
    }

    @Override
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}