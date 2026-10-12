package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.TestcontainersConfiguration;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Category;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.entity.OrderItem;
import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Datos de prueba:
 * - Ana:  orden PAID (Camisa x3 = 300) y orden CANCELLED (Gorra x10 = 500)
 * - Luis: orden DELIVERED (Camisa x2 + Gorra x1 = 250)
 * - Luis: orden RETURN_REQUESTED (Gorra x2 = 100): sigue contando como ingreso hasta aceptarse
 * - Ana:  orden PAID eliminada (soft delete, Camisa x50), que nunca debe contar
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PurchaseOrderStatsRepositoryTest {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(Category.builder().name("Ropa").build());
        Product shirt = productRepository.save(product("SKU-CAMISA", "Camisa", "100", category));
        Product cap = productRepository.save(product("SKU-GORRA", "Gorra", "50", category));

        Customer ana = customerRepository.save(customer("Ana", "ana@example.com"));
        Customer luis = customerRepository.save(customer("Luis", "luis@example.com"));
        Address anaAddress = addressRepository.save(address(ana));
        Address luisAddress = addressRepository.save(address(luis));

        saveOrder(ana, anaAddress, OrderStatus.PAID, false, item(shirt, 3));
        saveOrder(ana, anaAddress, OrderStatus.CANCELLED, false, item(cap, 10));
        saveOrder(luis, luisAddress, OrderStatus.DELIVERED, false, item(shirt, 2), item(cap, 1));
        saveOrder(luis, luisAddress, OrderStatus.RETURN_REQUESTED, false, item(cap, 2));
        saveOrder(ana, anaAddress, OrderStatus.PAID, true, item(shirt, 50));
    }

    @Test
    void monthlyIncomeShouldSumOnlyPaidShippedAndDeliveredByYearAndMonth() {
        List<Object[]> rows = purchaseOrderRepository.findMonthlyIncome();

        LocalDateTime now = LocalDateTime.now();
        assertEquals(1, rows.size());
        assertEquals(now.getYear(), ((Number) rows.get(0)[0]).intValue());
        assertEquals(now.getMonthValue(), ((Number) rows.get(0)[1]).intValue());
        assertEquals(0, new BigDecimal("650").compareTo((BigDecimal) rows.get(0)[2]));
    }

    @Test
    void bestSellingShouldIgnoreCancelledAndDeletedOrders() {
        List<Object[]> rows = purchaseOrderRepository.findBestSellingProducts();

        assertEquals(2, rows.size());
        assertEquals("Camisa", rows.get(0)[0]);
        assertEquals(5L, ((Number) rows.get(0)[1]).longValue());
        assertEquals("Gorra", rows.get(1)[0]);
        assertEquals(3L, ((Number) rows.get(1)[1]).longValue());
    }

    @Test
    void topCustomersShouldCountNonDeletedOrders() {
        Map<String, Long> ordersByCustomer = purchaseOrderRepository.findTopCustomers().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> ((Number) row[1]).longValue()));

        assertEquals(Map.of("Ana", 2L, "Luis", 2L), ordersByCustomer);
    }

    @Test
    void incomeByStatusShouldGroupEachStatus() {
        Map<String, BigDecimal> byStatus = purchaseOrderRepository.findIncomeByStatus().stream()
                .collect(Collectors.toMap(row -> row[0].toString(), row -> (BigDecimal) row[1]));

        assertEquals(4, byStatus.size());
        assertEquals(0, new BigDecimal("100").compareTo(byStatus.get("RETURN_REQUESTED")));
        assertEquals(0, new BigDecimal("300").compareTo(byStatus.get("PAID")));
        assertEquals(0, new BigDecimal("250").compareTo(byStatus.get("DELIVERED")));
        assertEquals(0, new BigDecimal("500").compareTo(byStatus.get("CANCELLED")));
    }

    private void saveOrder(Customer customer, Address address, OrderStatus status, boolean deleted, OrderItem... items) {
        PurchaseOrder order = PurchaseOrder.builder()
                .customer(customer)
                .address(address)
                .status(status)
                .total(BigDecimal.ZERO)
                .deletedAt(deleted ? LocalDateTime.now() : null)
                .build();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            item.setOrder(order);
            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotal(total);
        purchaseOrderRepository.save(order);
    }

    private static OrderItem item(Product product, int quantity) {
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    private static Product product(String sku, String name, String price, Category category) {
        return Product.builder().sku(sku).name(name).price(new BigDecimal(price)).active(true).category(category).build();
    }

    private static Customer customer(String name, String email) {
        return Customer.builder().name(name).email(email).status(CustomerStatus.ACTIVE).build();
    }

    private static Address address(Customer customer) {
        return Address.builder().street("Calle 10").city("Santa Marta").country("Colombia").customer(customer).build();
    }
}
