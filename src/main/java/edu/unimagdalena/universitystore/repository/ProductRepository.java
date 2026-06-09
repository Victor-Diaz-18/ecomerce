package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.sku = :sku")
    Optional<Product> findBySku(String sku);

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.category.id = :categoryId AND p.active = true")
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deletedAt = :now WHERE p.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.deletedAt IS NULL AND p.id = :id")
    boolean existsByIdNotDeleted(Long id);

    @Override
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAll();

    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    Page<Product> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"category"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.id = :id")
    Optional<Product> findById(Long id);
}
