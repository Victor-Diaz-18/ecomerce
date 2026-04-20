package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.InventoryDtos;
import edu.unimagdalena.universitystore.entity.Inventory;
import edu.unimagdalena.universitystore.entity.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-20T14:50:13-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public Inventory toEntity(InventoryDtos.CreateInventoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Inventory.InventoryBuilder inventory = Inventory.builder();

        inventory.availableStock( request.availableStock() );
        inventory.minimumStock( request.minimumStock() );

        return inventory.build();
    }

    @Override
    public InventoryDtos.InventoryResponse toResponse(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }

        Long productId = null;
        Long id = null;
        Integer availableStock = null;
        Integer minimumStock = null;

        productId = inventoryProductId( inventory );
        id = inventory.getId();
        availableStock = inventory.getAvailableStock();
        minimumStock = inventory.getMinimumStock();

        InventoryDtos.InventoryResponse inventoryResponse = new InventoryDtos.InventoryResponse( id, availableStock, minimumStock, productId );

        return inventoryResponse;
    }

    @Override
    public InventoryDtos.LowStockProductResponse toLowStockResponse(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }

        Long productId = null;
        String productName = null;
        Integer availableStock = null;
        Integer minimumStock = null;

        productId = inventoryProductId( inventory );
        productName = inventoryProductName( inventory );
        availableStock = inventory.getAvailableStock();
        minimumStock = inventory.getMinimumStock();

        InventoryDtos.LowStockProductResponse lowStockProductResponse = new InventoryDtos.LowStockProductResponse( productId, productName, availableStock, minimumStock );

        return lowStockProductResponse;
    }

    private Long inventoryProductId(Inventory inventory) {
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    private String inventoryProductName(Inventory inventory) {
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getName();
    }
}
