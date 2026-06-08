package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.OrderDtos.*;
import edu.unimagdalena.universitystore.enums.OrderStatus;

import java.util.List;

public interface PurchaseOrderService {
    OrderResponse create(CreateOrderRequest req);
    OrderResponse payOrder(Long orderId);
    OrderResponse shipOrder(Long orderId);
    OrderResponse deliverOrder(Long orderId);
    OrderResponse cancelOrder(Long orderId);
    OrderResponse returnOrder(Long orderId, String reason);
    List<OrderResponse> findAll();
    OrderResponse findById(Long id);
    List<OrderResponse> search(Long customerId, OrderStatus status);
    List<OrderStatusHistoryResponse> getHistory(Long orderId);
    void softDelete(Long orderId);
}