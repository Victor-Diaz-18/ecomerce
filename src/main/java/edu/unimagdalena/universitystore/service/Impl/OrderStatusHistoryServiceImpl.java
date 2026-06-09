package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.repository.OrderStatusHistoryRepository;
import edu.unimagdalena.universitystore.service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional
    public OrderStatusHistory create(OrderStatusHistory history) {
        history.setChangedAt(LocalDateTime.now());
        return orderStatusHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistory> findByOrder(Long orderId) {
        return orderStatusHistoryRepository.findByOrderId(orderId);
    }
}
