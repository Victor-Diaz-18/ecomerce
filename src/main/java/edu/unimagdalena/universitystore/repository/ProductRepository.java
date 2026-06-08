package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deletedAt = :now WHERE p.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL")
    List<Product> findAll();

    @Override
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.id = :id")
    Optional<Product> findById(Long id);
}