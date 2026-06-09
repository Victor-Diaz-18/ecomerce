package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.ProductPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductPriceHistoryRepository extends JpaRepository<ProductPriceHistory, Long> {
    @Query("SELECT pph FROM ProductPriceHistory pph WHERE pph.deletedAt IS NULL AND pph.product.id = :productId")
    List<ProductPriceHistory> findByProductId(Long productId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductPriceHistory pph SET pph.deletedAt = :now WHERE pph.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT pph FROM ProductPriceHistory pph WHERE pph.deletedAt IS NULL")
    List<ProductPriceHistory> findAll();

    @Override
    @Query("SELECT pph FROM ProductPriceHistory pph WHERE pph.deletedAt IS NULL AND pph.id = :id")
    Optional<ProductPriceHistory> findById(Long id);
}
