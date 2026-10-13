package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.ProductPriceHistory;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.CategoryRepository;
import edu.unimagdalena.universitystore.repository.ProductPriceHistoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;

    @Override
    @Transactional
    public Product create(Product product) {
        if (product.getSku() != null && productRepository.findBySku(product.getSku()).isPresent()) {
            throw new ConflictException("SKU already exists");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);

        if (product.getActive() == null) {
            product.setActive(true);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        Product existing = findById(id);

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }

        if (product.getSku() != null && !product.getSku().equals(existing.getSku())
                && productRepository.findBySku(product.getSku()).isPresent()) {
            throw new ConflictException("SKU already exists");
        }

        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            existing.setCategory(category);
        }

        if (!existing.getPrice().equals(product.getPrice())) {
            ProductPriceHistory history = ProductPriceHistory.builder()
                    .product(existing)
                    .oldPrice(existing.getPrice())
                    .newPrice(product.getPrice())
                    .changedAt(LocalDateTime.now())
                    .build();
            priceHistoryRepository.save(history);
        }

        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getSku() != null) {
            existing.setSku(product.getSku());
        }
        existing.setPrice(product.getPrice());
        if (product.getActive() != null) {
            existing.setActive(product.getActive());
        }

        return productRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        product.softDelete();
        productRepository.save(product);
    }
}
