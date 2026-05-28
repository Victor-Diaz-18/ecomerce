package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.TestcontainersConfiguration;
import edu.unimagdalena.universitystore.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSaveCategory() {
        Category category = new Category();
        category.setName("Electrónica");

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Electrónica");
    }

    @Test
    void shouldFindCategoryById() {
        Category category = new Category();
        category.setName("Libros");

        Category savedCategory = categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Libros");
    }

    @Test
    void shouldDeleteCategory() {
        Category category = new Category();
        category.setName("Ropa");

        Category savedCategory = categoryRepository.save(category);

        categoryRepository.delete(savedCategory);

        Optional<Category> result = categoryRepository.findById(savedCategory.getId());
        assertThat(result).isEmpty();
    }
}