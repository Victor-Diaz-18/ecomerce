package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.dto.AccountDtos.CreateMyOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.CreateOrderRequest;
import edu.unimagdalena.universitystore.dto.OrderDtos.OrderResponse;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final CustomerService customerService;
    private final AddressService addressService;
    private final PurchaseOrderService orderService;

    public Customer profile(Long customerId) {
        return customerService.findById(customerId);
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

    @Transactional
    public OrderResponse createOrder(Long customerId, CreateMyOrderRequest request) {
        ownedAddress(customerId, request.addressId());
        return orderService.create(new CreateOrderRequest(customerId, request.addressId(), request.items()));
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
