package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "addressLine", expression = "java(order.getAddress() != null ? order.getAddress().getStreet() + \", \" + order.getAddress().getCity() + \", \" + order.getAddress().getCountry() : null)")
    @Mapping(target = "status", expression = "java(order.getStatus() != null ? order.getStatus().name() : null)")
    @Mapping(target = "items", qualifiedByName = "mapItems")
    OrderDtos.OrderResponse toResponse(PurchaseOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderDtos.OrderItemResponse toItemResponse(OrderItem item);

    List<OrderDtos.OrderItemResponse> toItemResponseList(List<OrderItem> items);

    @Mapping(target = "status", expression = "java(history.getStatus() != null ? history.getStatus().name() : null)")
    OrderDtos.OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    List<OrderDtos.OrderStatusHistoryResponse> toHistoryResponseList(List<OrderStatusHistory> history);

    @org.mapstruct.Named("mapItems")
    default List<OrderDtos.OrderItemResponse> mapItems(List<OrderItem> items) {
        return items == null ? List.of() : items.stream()
                .map(this::toItemResponse)
                .toList();
    }
}