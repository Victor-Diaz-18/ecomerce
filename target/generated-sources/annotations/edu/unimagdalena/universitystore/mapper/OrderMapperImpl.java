package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.OrderStatusHistory;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T00:27:21-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDtos.OrderResponse toResponse(PurchaseOrder order) {
        if ( order == null ) {
            return null;
        }

        Long customerId = null;
        String customerName = null;
        Long addressId = null;
        List<OrderDtos.OrderItemResponse> items = null;
        Long id = null;
        LocalDateTime createdAt = null;
        BigDecimal total = null;

        customerId = orderCustomerId( order );
        customerName = orderCustomerName( order );
        addressId = orderAddressId( order );
        items = mapItems( order.getItems() );
        id = order.getId();
        createdAt = order.getCreatedAt();
        total = order.getTotal();

        String addressLine = order.getAddress() != null ? order.getAddress().getStreet() + ", " + order.getAddress().getCity() + ", " + order.getAddress().getCountry() : null;
        String status = order.getStatus() != null ? order.getStatus().name() : null;

        OrderDtos.OrderResponse orderResponse = new OrderDtos.OrderResponse( id, status, createdAt, customerId, customerName, addressId, addressLine, items, total );

        return orderResponse;
    }

    @Override
    public OrderDtos.OrderItemResponse toItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        Long productId = null;
        String productName = null;
        Long id = null;
        Integer quantity = null;
        BigDecimal unitPrice = null;
        BigDecimal subtotal = null;

        productId = itemProductId( item );
        productName = itemProductName( item );
        id = item.getId();
        quantity = item.getQuantity();
        unitPrice = item.getUnitPrice();
        subtotal = item.getSubtotal();

        OrderDtos.OrderItemResponse orderItemResponse = new OrderDtos.OrderItemResponse( id, productId, productName, quantity, unitPrice, subtotal );

        return orderItemResponse;
    }

    @Override
    public List<OrderDtos.OrderItemResponse> toItemResponseList(List<OrderItem> items) {
        if ( items == null ) {
            return null;
        }

        List<OrderDtos.OrderItemResponse> list = new ArrayList<OrderDtos.OrderItemResponse>( items.size() );
        for ( OrderItem orderItem : items ) {
            list.add( toItemResponse( orderItem ) );
        }

        return list;
    }

    @Override
    public OrderDtos.OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history) {
        if ( history == null ) {
            return null;
        }

        Long id = null;
        LocalDateTime changedAt = null;

        id = history.getId();
        changedAt = history.getChangedAt();

        String status = history.getStatus() != null ? history.getStatus().name() : null;

        OrderDtos.OrderStatusHistoryResponse orderStatusHistoryResponse = new OrderDtos.OrderStatusHistoryResponse( id, status, changedAt );

        return orderStatusHistoryResponse;
    }

    @Override
    public List<OrderDtos.OrderStatusHistoryResponse> toHistoryResponseList(List<OrderStatusHistory> history) {
        if ( history == null ) {
            return null;
        }

        List<OrderDtos.OrderStatusHistoryResponse> list = new ArrayList<OrderDtos.OrderStatusHistoryResponse>( history.size() );
        for ( OrderStatusHistory orderStatusHistory : history ) {
            list.add( toHistoryResponse( orderStatusHistory ) );
        }

        return list;
    }

    private Long orderCustomerId(PurchaseOrder purchaseOrder) {
        Customer customer = purchaseOrder.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }

    private String orderCustomerName(PurchaseOrder purchaseOrder) {
        Customer customer = purchaseOrder.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getName();
    }

    private Long orderAddressId(PurchaseOrder purchaseOrder) {
        Address address = purchaseOrder.getAddress();
        if ( address == null ) {
            return null;
        }
        return address.getId();
    }

    private Long itemProductId(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    private String itemProductName(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getName();
    }
}
