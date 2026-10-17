package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.ProductImage;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.ProductImageRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

/**
 * Subir, mostrar y quitar la foto de un producto. Al cambiarla se actualiza la "versión" de la foto en
 * el producto, que el frontend agrega a la dirección de la imagen para que el navegador no muestre la vieja.
 */
@Service
@RequiredArgsConstructor
public class ProductImageService {
    public static final long MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void save(Long productId, String contentType, byte[] data) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException("La foto debe ser JPG, PNG o WebP");
        }
        if (data == null || data.length == 0) {
            throw new ValidationException("La foto está vacía");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("La foto no puede pesar más de 2 MB");
        }

        ProductImage image = imageRepository.findByProductId(productId)
                .orElseGet(() -> ProductImage.builder().productId(productId).build());
        image.setContentType(contentType);
        image.setData(data);
        imageRepository.save(image);

        product.setImageVersion(Instant.now().toEpochMilli());
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public ProductImage find(Long productId) {
        return imageRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found"));
    }

    @Transactional
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        imageRepository.findByProductId(productId).ifPresent(imageRepository::delete);
        product.setImageVersion(null);
        productRepository.save(product);
    }
}
