package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.InventoryDtos;
import edu.unimagdalena.universitystore.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toEntity(InventoryDtos.CreateInventoryRequest request);

    @Mapping(target = "productId", source = "product.id")
    InventoryDtos.InventoryResponse toResponse(Inventory inventory);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    InventoryDtos.LowStockProductResponse toLowStockResponse(Inventory inventory);
}