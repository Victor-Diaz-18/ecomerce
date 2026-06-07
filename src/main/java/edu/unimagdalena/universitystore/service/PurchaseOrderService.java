package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrder create(PurchaseOrder order);
    PurchaseOrder payOrder(Long orderId);
    PurchaseOrder shipOrder(Long orderId);
    PurchaseOrder deliverOrder(Long orderId);
    PurchaseOrder cancelOrder(Long orderId);
    List<PurchaseOrder> findAll();
    PurchaseOrder findById(Long id);
    List<PurchaseOrder> search(Long customerId, OrderStatus status);
}
