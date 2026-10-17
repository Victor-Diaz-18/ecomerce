package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByProductId(Long productId);
}
