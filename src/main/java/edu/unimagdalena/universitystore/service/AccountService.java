package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.AccountDtos.BuyNowRequest;
import edu.unimagdalena.universitystore.dto.AccountDtos.CreateMyOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderItemRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderStatusHistoryResponse;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lo que un cliente puede hacer con su propia cuenta. Cada operación recibe el id del cliente que
 * inició sesión y comprueba que la orden o la dirección sea suya; si no lo es responde igual que si
 * no existiera, para no revelar datos de otros clientes.
 */
@Service
@RequiredArgsConstructor
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final CustomerService customerService;
    private final AddressService addressService;
    private final PurchaseOrderService orderService;

    public Customer profile(Long customerId) {
        return customerService.findById(customerId);
    }

    public Customer updateName(Long customerId, String name) {
        Customer changes = new Customer();
        changes.setName(name.trim());
        return customerService.update(customerId, changes);
    }

    public List<Address> addresses(Long customerId) {
        return addressService.findByCustomer(customerId);
    }

    public Address addAddress(Long customerId, String street, String neighborhood, String city) {
        Customer owner = new Customer();
        owner.setId(customerId);

        Address address = new Address();
        address.setStreet(street);
        address.setNeighborhood(neighborhood);
        address.setCity(city);
        address.setCustomer(owner);
        return addressService.create(address);
    }

    @Transactional
    public Address updateAddress(Long customerId, Long addressId, String street, String neighborhood, String city) {
        ownedAddress(customerId, addressId);
        return addressService.update(addressId, street, neighborhood, city);
    }

    public List<OrderResponse> orders(Long customerId) {
        return orderService.search(customerId, null);
    }

    public OrderResponse order(Long customerId, Long orderId) {
        return ownedOrder(customerId, orderId);
    }

    public List<OrderStatusHistoryResponse> orderHistory(Long customerId, Long orderId) {
        ownedOrder(customerId, orderId);
        return orderService.getHistory(orderId);
    }

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateMyOrderRequest request) {
        ownedAddress(customerId, request.addressId());
        return orderService.create(new CreateOrderRequest(customerId, request.addressId(), request.items()));
    }

    /**
     * "Comprar ahora": crea la orden y la paga de una vez. Son dos pasos separados a propósito: si el
     * cobro falla, la orden ya creada queda pendiente de pago (con sus unidades separadas) para que el
     * cliente reintente desde Mis órdenes, en vez de perder la compra. Si lo que falla es la creación
     * (sin stock, dirección inválida), no queda nada y el error llega al cliente.
     */
    public OrderResponse buyNow(Long customerId, BuyNowRequest request) {
        ownedAddress(customerId, request.addressId());
        OrderResponse created = orderService.create(new CreateOrderRequest(customerId, request.addressId(),
                List.of(new CreateOrderItemRequest(request.productId(), request.quantity()))));
        try {
            return orderService.payOrder(created.id());
        } catch (RuntimeException e) {
            log.warn("No se pudo cobrar la orden {}; queda pendiente de pago: {}", created.id(), e.getMessage());
            return orderService.findById(created.id());
        }
    }

    @Transactional
    public OrderResponse payOrder(Long customerId, Long orderId) {
        ownedOrder(customerId, orderId);
        return orderService.payOrder(orderId);
    }

    @Transactional
    public OrderResponse cancelOrder(Long customerId, Long orderId) {
        ownedOrder(customerId, orderId);
        return orderService.cancelOrder(orderId);
    }

    @Transactional
    public OrderResponse returnOrder(Long customerId, Long orderId, String reason) {
        ownedOrder(customerId, orderId);
        return orderService.returnOrder(orderId, reason);
    }

    // changeAddress itself rejects an address that belongs to another customer
    @Transactional
    public OrderResponse changeOrderAddress(Long customerId, Long orderId, Long addressId) {
        ownedOrder(customerId, orderId);
        return orderService.changeAddress(orderId, addressId);
    }

    private OrderResponse ownedOrder(Long customerId, Long orderId) {
        OrderResponse order = orderService.findById(orderId);
        if (!customerId.equals(order.customerId())) {
            throw new ResourceNotFoundException("Order not found");
        }
        return order;
    }

    private Address ownedAddress(Long customerId, Long addressId) {
        Address address = addressService.findById(addressId);
        if (address.getCustomer() == null || !customerId.equals(address.getCustomer().getId())) {
            throw new ResourceNotFoundException("Address not found");
        }
        return address;
    }
}
