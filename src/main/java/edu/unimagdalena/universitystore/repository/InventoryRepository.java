package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @EntityGraph(attributePaths = {"product"})
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.deletedAt IS NULL
        AND i.availableStock < i.minimumStock
    """)
    List<Inventory> findLowStockProducts();

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT i FROM Inventory i WHERE i.deletedAt IS NULL AND i.product.id = :productId")
    Optional<Inventory> findByProductId(long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.deletedAt IS NULL AND i.product.id = :productId")
    Optional<Inventory> findByProductIdForUpdate(Long productId);

    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.deletedAt = :now WHERE i.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i WHERE i.deletedAt IS NULL AND i.id = :id")
    boolean existsByIdNotDeleted(Long id);

    @Override
    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT i FROM Inventory i WHERE i.deletedAt IS NULL")
    List<Inventory> findAll();

    @Override
    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT i FROM Inventory i WHERE i.deletedAt IS NULL AND i.id = :id")
    Optional<Inventory> findById(Long id);
}
