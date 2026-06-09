package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @EntityGraph(attributePaths = {"customer", "address", "items"})
    @Query("""
        SELECT o FROM PurchaseOrder o
        WHERE o.deletedAt IS NULL
        AND (:customerId IS NULL OR o.customer.id = :customerId)
        AND (:status IS NULL OR o.status = :status)
    """)
    List<PurchaseOrder> searchOrders(Long customerId, OrderStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE PurchaseOrder o SET o.deletedAt = :now WHERE o.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM PurchaseOrder o WHERE o.deletedAt IS NULL AND o.id = :id")
    boolean existsByIdNotDeleted(Long id);

    @Override
    @EntityGraph(attributePaths = {"customer", "address", "items"})
    @Query("SELECT o FROM PurchaseOrder o WHERE o.deletedAt IS NULL")
    List<PurchaseOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "address", "items"})
    @Query("SELECT o FROM PurchaseOrder o WHERE o.deletedAt IS NULL AND o.id = :id")
    Optional<PurchaseOrder> findById(Long id);
}
