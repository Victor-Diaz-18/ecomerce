package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Category;

import java.util.List;

public interface CategoryService {
    Category create(Category category);
    List<Category> findAll();
    Category findById(Long id);
}