package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.OrderStatusHistory;

import java.util.List;

public interface OrderStatusHistoryService {
    OrderStatusHistory create(OrderStatusHistory history);
    List<OrderStatusHistory> findByOrder(Long orderId);
}