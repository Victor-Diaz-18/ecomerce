package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.ProductDtos;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Product;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-20T14:50:13-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(ProductDtos.CreateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.category( createProductRequestToCategory( request ) );
        product.sku( request.sku() );
        product.name( request.name() );
        product.price( request.price() );

        return product.build();
    }

    @Override
    public Product toEntity(ProductDtos.UpdateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( request.name() );
        product.price( request.price() );
        product.active( request.active() );

        return product.build();
    }

    @Override
    public ProductDtos.ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        Long categoryId = null;
        Long id = null;
        String name = null;
        String sku = null;
        BigDecimal price = null;
        Boolean active = null;

        categoryId = productCategoryId( product );
        id = product.getId();
        name = product.getName();
        sku = product.getSku();
        price = product.getPrice();
        active = product.getActive();

        ProductDtos.ProductResponse productResponse = new ProductDtos.ProductResponse( id, name, sku, price, active, categoryId );

        return productResponse;
    }

    protected Category createProductRequestToCategory(ProductDtos.CreateProductRequest createProductRequest) {
        if ( createProductRequest == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.id( createProductRequest.categoryId() );

        return category.build();
    }

    private Long productCategoryId(Product product) {
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        return category.getId();
    }
}
