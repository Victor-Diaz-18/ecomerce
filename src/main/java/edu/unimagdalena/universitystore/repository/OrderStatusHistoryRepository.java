package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderId(Long orderId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderStatusHistory osh SET osh.deletedAt = :now WHERE osh.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.deletedAt IS NULL")
    List<OrderStatusHistory> findAll();

    @Override
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.deletedAt IS NULL AND osh.id = :id")
    Optional<OrderStatusHistory> findById(Long id);
}