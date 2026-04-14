package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.repository.CategoryRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import edu.unimagdalena.universitystore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import edu.unimagdalena.universitystore.exception.BusinessException;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;

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
            throw new ConflictException("SKU already exists");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Price must be greater than zero");
        }
        if (!categoryRepository.existsById(product.getCategory().getId())) {
            throw new ResourceNotFoundException("Category not found");
        }
        return productRepository.save(product);
    }
    @Override
    public Product update(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setSku(product.getSku());
        existing.setActive(product.getActive());

        if (product.getCategory() != null) {
            existing.setCategory(product.getCategory());
        }

        return productRepository.save(existing);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }
}