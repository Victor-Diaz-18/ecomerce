package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CategoryDtos;
import edu.unimagdalena.universitystore.entity.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {
    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void shouldMapEntityToResponse() {
        Category category = Category.builder().id(1L).name("Ropa").build();

        CategoryDtos.CategoryResponse result = mapper.toResponse(category);

        assertEquals(1L, result.id());
        assertEquals("Ropa", result.name());
    }
}
