package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T00:27:21-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductDtos.ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        Long categoryId = null;
        String categoryName = null;
        Long id = null;
        String name = null;
        String sku = null;
        BigDecimal price = null;
        Boolean active = null;

        categoryId = productCategoryId( product );
        categoryName = productCategoryName( product );
        id = product.getId();
        name = product.getName();
        sku = product.getSku();
        price = product.getPrice();
        active = product.getActive();

        ProductDtos.ProductResponse productResponse = new ProductDtos.ProductResponse( id, name, sku, price, active, categoryId, categoryName );

        return productResponse;
    }

    private Long productCategoryId(Product product) {
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }

    private String productCategoryName(Product product) {
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getName();
    }
}
