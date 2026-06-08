package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Modifying
    @Transactional
    @Query("UPDATE OrderItem oi SET oi.deletedAt = :now WHERE oi.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT oi FROM OrderItem oi WHERE oi.deletedAt IS NULL")
    List<OrderItem> findAll();

    @Override
    @Query("SELECT oi FROM OrderItem oi WHERE oi.deletedAt IS NULL AND oi.id = :id")
    Optional<OrderItem> findById(Long id);
}