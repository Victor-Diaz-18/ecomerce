package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.repository.OrderItemRepository;
import edu.unimagdalena.universitystore.service.Impl.OrderItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void shouldCreateOrderItemAndCalculateSubtotal() {
        OrderItem orderItem = OrderItem.builder()
                .quantity(2)
                .unitPrice(new BigDecimal("50000"))
                .build();

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderItem result = orderItemService.create(orderItem);

        assertEquals(new BigDecimal("100000"), result.getSubtotal());
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void shouldFindOrderItemsByOrderId() {
        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of(new OrderItem(), new OrderItem()));

        List<OrderItem> result = orderItemService.findByOrder(1L);

        assertEquals(2, result.size());
    }
}