package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("""
        SELECT o FROM PurchaseOrder o
        WHERE (:customerId IS NULL OR o.customer.id = :customerId)
        AND (:status IS NULL OR o.status = :status)
    """)
    List<PurchaseOrder> searchOrders(Long customerId, OrderStatus status);
}