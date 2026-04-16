package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrder create(PurchaseOrder order);
    PurchaseOrder payOrder(Long orderId);
    List<PurchaseOrder> findAll();
    PurchaseOrder findById(Long id);
}