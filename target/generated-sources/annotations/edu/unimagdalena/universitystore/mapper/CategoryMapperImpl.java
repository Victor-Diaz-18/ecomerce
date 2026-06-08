package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CategoryDtos;
import edu.unimagdalena.universitystore.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T00:27:21-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public CategoryDtos.CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        Long id = null;
        String name = null;

        id = category.getId();
        name = category.getName();

        CategoryDtos.CategoryResponse categoryResponse = new CategoryDtos.CategoryResponse( id, name );

        return categoryResponse;
    }
}
