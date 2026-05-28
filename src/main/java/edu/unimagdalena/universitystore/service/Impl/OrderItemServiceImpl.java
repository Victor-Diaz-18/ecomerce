package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItem create(OrderItem orderItem) {
        BigDecimal subtotal = orderItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        orderItem.setSubtotal(subtotal);

        return orderItemRepository.save(orderItem);
    }

    @Override
    public List<OrderItem> findByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}