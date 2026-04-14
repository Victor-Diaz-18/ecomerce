package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Product toEntity(ProductDtos.CreateProductRequest request);

    Product toEntity(ProductDtos.UpdateProductRequest request);
    @Mapping(target = "categoryId", source = "category.id")
    ProductDtos.ProductResponse toResponse(Product product);
}