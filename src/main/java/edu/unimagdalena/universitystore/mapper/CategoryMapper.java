package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CategoryDtos;
import edu.unimagdalena.universitystore.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryDtos.CategoryResponse toResponse(Category category);
}