package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Product toEntity(ProductDtos.CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDtos.UpdateProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    ProductDtos.ProductResponse toResponse(Product product);
}