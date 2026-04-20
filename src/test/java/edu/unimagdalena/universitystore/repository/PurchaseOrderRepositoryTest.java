package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.TestcontainersConfiguration;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseOrderRepositoryTest {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldSearchOrdersByCustomerAndStatus() {
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Juan Perez")
                        .email("juan@example.com")
                        .status(CustomerStatus.ACTIVE)
                        .build()
        );

        Address address = addressRepository.save(
                Address.builder()
                        .street("Calle 10")
                        .city("Santa Marta")
                        .country("Colombia")
                        .customer(customer)
                        .build()
        );

        purchaseOrderRepository.save(
                PurchaseOrder.builder()
                        .createdAt(LocalDateTime.now())
                        .total(new BigDecimal("150000"))
                        .status(OrderStatus.CREATED)
                        .customer(customer)
                        .address(address)
                        .build()
        );

        List<PurchaseOrder> result = purchaseOrderRepository.searchOrders(
                customer.getId(),
                OrderStatus.CREATED
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomer().getId());
        assertEquals(OrderStatus.CREATED, result.get(0).getStatus());
    }
}