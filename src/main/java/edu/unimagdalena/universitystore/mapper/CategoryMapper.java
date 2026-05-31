package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CategoryDtos;
import edu.unimagdalena.universitystore.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryDtos.CreateCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryDtos.UpdateCategoryRequest request);

    CategoryDtos.CategoryResponse toResponse(Category category);
}
