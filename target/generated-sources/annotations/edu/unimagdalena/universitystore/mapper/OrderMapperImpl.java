package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.OrderDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.OrderItem;
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
    date = "2026-05-28T12:23:34-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public PurchaseOrder toEntity(OrderDtos.CreateOrderRequest request) {
        if ( request == null ) {
            return null;
        }

        PurchaseOrder.PurchaseOrderBuilder purchaseOrder = PurchaseOrder.builder();

        purchaseOrder.customer( createOrderRequestToCustomer( request ) );
        purchaseOrder.address( createOrderRequestToAddress( request ) );
        purchaseOrder.items( createOrderItemRequestListToOrderItemList( request.items() ) );

        return purchaseOrder.build();
    }

    @Override
    public OrderItem toItemEntity(OrderDtos.CreateOrderItemRequest request) {
        if ( request == null ) {
            return null;
        }

        OrderItem.OrderItemBuilder orderItem = OrderItem.builder();

        orderItem.product( createOrderItemRequestToProduct( request ) );
        orderItem.quantity( request.quantity() );

        return orderItem.build();
    }

    @Override
    public OrderDtos.OrderResponse toResponse(PurchaseOrder order) {
        if ( order == null ) {
            return null;
        }

        Long customerId = null;
        String status = null;
        Long id = null;
        LocalDateTime createdAt = null;
        List<OrderDtos.OrderItemResponse> items = null;
        BigDecimal total = null;

        customerId = orderCustomerId( order );
        if ( order.getStatus() != null ) {
            status = order.getStatus().name();
        }
        id = order.getId();
        createdAt = order.getCreatedAt();
        items = toItemResponseList( order.getItems() );
        total = order.getTotal();

        OrderDtos.OrderResponse orderResponse = new OrderDtos.OrderResponse( id, status, createdAt, customerId, items, total );

        return orderResponse;
    }

    @Override
    public OrderDtos.OrderItemResponse toItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        Long productId = null;
        Long id = null;
        Integer quantity = null;
        BigDecimal unitPrice = null;
        BigDecimal subtotal = null;

        productId = itemProductId( item );
        id = item.getId();
        quantity = item.getQuantity();
        unitPrice = item.getUnitPrice();
        subtotal = item.getSubtotal();

        OrderDtos.OrderItemResponse orderItemResponse = new OrderDtos.OrderItemResponse( id, productId, quantity, unitPrice, subtotal );

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

    protected Customer createOrderRequestToCustomer(OrderDtos.CreateOrderRequest createOrderRequest) {
        if ( createOrderRequest == null ) {
            return null;
        }

        Customer.CustomerBuilder customer = Customer.builder();

        customer.id( createOrderRequest.customerId() );

        return customer.build();
    }

    protected Address createOrderRequestToAddress(OrderDtos.CreateOrderRequest createOrderRequest) {
        if ( createOrderRequest == null ) {
            return null;
        }

        Address.AddressBuilder address = Address.builder();

        address.id( createOrderRequest.addressId() );

        return address.build();
    }

    protected List<OrderItem> createOrderItemRequestListToOrderItemList(List<OrderDtos.CreateOrderItemRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItem> list1 = new ArrayList<OrderItem>( list.size() );
        for ( OrderDtos.CreateOrderItemRequest createOrderItemRequest : list ) {
            list1.add( toItemEntity( createOrderItemRequest ) );
        }

        return list1;
    }

    protected Product createOrderItemRequestToProduct(OrderDtos.CreateOrderItemRequest createOrderItemRequest) {
        if ( createOrderItemRequest == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.id( createOrderItemRequest.productId() );

        return product.build();
    }

    private Long orderCustomerId(PurchaseOrder purchaseOrder) {
        Customer customer = purchaseOrder.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }

    private Long itemProductId(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }
}
