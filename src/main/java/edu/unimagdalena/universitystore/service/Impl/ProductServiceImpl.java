package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.repository.CategoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product create(Product product) {
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("SKU already exists");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }
        if (!categoryRepository.existsById(product.getCategory().getId())) {
            throw new RuntimeException("Category not found");
        }

        product.setActive(true);

        return productRepository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }
}