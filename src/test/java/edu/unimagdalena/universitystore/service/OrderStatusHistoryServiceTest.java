package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.repository.OrderStatusHistoryRepository;
import edu.unimagdalena.universitystore.service.Impl.OrderStatusHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusHistoryServiceImplTest {
    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks
    private OrderStatusHistoryServiceImpl orderStatusHistoryService;

    @Test
    void shouldCreateHistoryAndSetChangedAt() {
        OrderStatusHistory history = OrderStatusHistory.builder().build();

        when(orderStatusHistoryRepository.save(any(OrderStatusHistory.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderStatusHistory result = orderStatusHistoryService.create(history);

        assertNotNull(result.getChangedAt());
        verify(orderStatusHistoryRepository).save(history);
    }

    @Test
    void shouldFindHistoryByOrderId() {
        when(orderStatusHistoryRepository.findByOrderId(1L))
                .thenReturn(List.of(new OrderStatusHistory(), new OrderStatusHistory()));

        List<OrderStatusHistory> result = orderStatusHistoryService.findByOrder(1L);

        assertEquals(2, result.size());
    }
}