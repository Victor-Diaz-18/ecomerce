package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public OrderItem create(OrderItem orderItem) {
        orderItem.calculateSubtotal();
        return orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> findByOrder(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
