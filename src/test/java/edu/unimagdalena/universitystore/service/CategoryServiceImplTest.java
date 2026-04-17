package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.repository.CategoryRepository;
import edu.unimagdalena.universitystore.service.Impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void shouldCreateCategory() {
        Category category = Category.builder()
                .name("Electronics")
                .build();

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.create(category);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldFindAllCategories() {
        when(categoryRepository.findAll())
                .thenReturn(List.of(new Category(), new Category()));

        List<Category> result = categoryService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindCategoryById() {
        Category category = Category.builder()
                .id(1L)
                .name("Books")
                .build();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Books", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryService.findById(1L));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        Category category = Category.builder()
                .name("Electronics")
                .build();

        when(categoryRepository.findByName("Electronics"))
                .thenReturn(Optional.of(category));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryService.create(category));

        assertEquals("Category already exists", exception.getMessage());
    }
}