package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product create(Product product);
    Page<Product> findAll(Pageable pageable);
    List<Product> findAll();
    Product findById(Long id);
    Product update(Long id, Product product);
    void delete(Long id);
    List<Product> findByCategory(Long categoryId);
}