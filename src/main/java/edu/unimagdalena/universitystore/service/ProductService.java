package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Product;

import java.util.List;

public interface ProductService {
    Product create(Product product);
    List<Product> findAll();
    Product findById(Long id);
    List<Product> findByCategory(Long categoryId);
}