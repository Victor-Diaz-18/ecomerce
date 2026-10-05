package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Query("""
        SELECT EXTRACT(YEAR FROM o.createdAt) as year, EXTRACT(MONTH FROM o.createdAt) as month, SUM(o.total) as totalIncome
        FROM PurchaseOrder o
        WHERE o.deletedAt IS NULL
        AND o.status IN ('PAID', 'SHIPPED', 'DELIVERED')
        GROUP BY EXTRACT(YEAR FROM o.createdAt), EXTRACT(MONTH FROM o.createdAt)
        ORDER BY year, month
    """)
    List<Object[]> findMonthlyIncome();

    @Query("""
        SELECT p.name as productName, SUM(oi.quantity) as totalSold
        FROM PurchaseOrder o
        JOIN o.items oi
        JOIN oi.product p
        WHERE o.deletedAt IS NULL
        AND o.status IN ('PAID', 'SHIPPED', 'DELIVERED')
        GROUP BY p.name
        ORDER BY totalSold DESC
    """)
    List<Object[]> findBestSellingProducts();

    @Query("""
        SELECT c.name as customerName, COUNT(o) as totalOrders
        FROM PurchaseOrder o
        JOIN o.customer c
        WHERE o.deletedAt IS NULL
        GROUP BY c.name
        ORDER BY totalOrders DESC
    """)
    List<Object[]> findTopCustomers();

    @Query("""
        SELECT o.status, SUM(o.total) as totalIncome
        FROM PurchaseOrder o
        WHERE o.deletedAt IS NULL
        AND o.status IN ('PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED')
        GROUP BY o.status
    """)
    List<Object[]> findIncomeByStatus();
}
