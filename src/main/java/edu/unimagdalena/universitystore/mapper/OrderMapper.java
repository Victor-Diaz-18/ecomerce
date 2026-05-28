package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "address.id", source = "addressId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "items", source = "items")
    PurchaseOrder toEntity(OrderDtos.CreateOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    OrderItem toItemEntity(OrderDtos.CreateOrderItemRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "status", source = "status")
    OrderDtos.OrderResponse toResponse(PurchaseOrder order);

    @Mapping(target = "productId", source = "product.id")
    OrderDtos.OrderItemResponse toItemResponse(OrderItem item);

    List<OrderDtos.OrderItemResponse> toItemResponseList(List<OrderItem> items);
}
