package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.TestcontainersConfiguration;
import edu.unimagdalena.universitystore.entity.*;
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
class OrderStatusHistoryRepositoryTest {
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindHistoryByOrderId() {
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Pedro")
                        .email("pedro@test.com")
                        .status(CustomerStatus.ACTIVE)
                        .build()
        );

        Address address = addressRepository.save(
                Address.builder()
                        .street("Street 30")
                        .city("Santa Marta")
                        .country("Colombia")
                        .customer(customer)
                        .build()
        );

        PurchaseOrder order = purchaseOrderRepository.save(
                PurchaseOrder.builder()
                        .createdAt(LocalDateTime.now())
                        .total(new BigDecimal("300"))
                        .status(OrderStatus.CREATED)
                        .customer(customer)
                        .address(address)
                        .build()
        );

        orderStatusHistoryRepository.save(
                OrderStatusHistory.builder()
                        .status(OrderStatus.CREATED)
                        .changedAt(LocalDateTime.now())
                        .order(order)
                        .build()
        );

        List<OrderStatusHistory> result =
                orderStatusHistoryRepository.findByOrderId(order.getId());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}