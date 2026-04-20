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
class OrderItemRepositoryTest {
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void shouldFindItemsByOrderId() {
        Category category = categoryRepository.save(
                Category.builder()
                        .name("Electronics")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .sku("SKU100")
                        .name("Keyboard")
                        .price(new BigDecimal("100"))
                        .active(true)
                        .category(category)
                        .build()
        );

        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Ana")
                        .email("ana@test.com")
                        .status(CustomerStatus.ACTIVE)
                        .build()
        );

        Address address = addressRepository.save(
                Address.builder()
                        .street("Street 20")
                        .city("Santa Marta")
                        .country("Colombia")
                        .customer(customer)
                        .build()
        );

        PurchaseOrder order = purchaseOrderRepository.save(
                PurchaseOrder.builder()
                        .createdAt(LocalDateTime.now())
                        .total(new BigDecimal("100"))
                        .status(OrderStatus.CREATED)
                        .customer(customer)
                        .address(address)
                        .build()
        );

        orderItemRepository.save(
                OrderItem.builder()
                        .quantity(1)
                        .unitPrice(new BigDecimal("100"))
                        .subtotal(new BigDecimal("100"))
                        .order(order)
                        .product(product)
                        .build()
        );

        List<OrderItem> result = orderItemRepository.findByOrderId(order.getId());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}