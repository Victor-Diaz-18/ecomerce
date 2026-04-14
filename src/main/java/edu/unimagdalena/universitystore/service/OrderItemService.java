package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    OrderItem create(OrderItem orderItem);
    List<OrderItem> findByOrder(Long orderId);
}