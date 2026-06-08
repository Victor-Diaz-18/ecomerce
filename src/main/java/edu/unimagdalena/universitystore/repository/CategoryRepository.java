package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.deletedAt = :now WHERE c.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAll();

    @Override
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL AND c.id = :id")
    Optional<Category> findById(Long id);
}