package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveCategory() {
        Category category = new Category();
        category.setName("Electronics");

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldFindCategoryById() {
        Category category = new Category();
        category.setName("Books");

        Category savedCategory = categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Books");
    }

    @Test
    void shouldDeleteCategory() {
        Category category = new Category();
        category.setName("Clothes");

        Category savedCategory = categoryRepository.save(category);

        categoryRepository.delete(savedCategory);

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindCategoryByName() {
        Category category = new Category();
        category.setName("Electronics");

        categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findByName("Electronics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }
}