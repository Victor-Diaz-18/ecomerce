package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindCustomerByEmail() {
        customerRepository.save(
                Customer.builder()
                        .name("Juan")
                        .email("juan@test.com")
                        .status(CustomerStatus.ACTIVE)
                        .build()
        );

        Optional<Customer> found = customerRepository.findByEmail("juan@test.com");

        assertTrue(found.isPresent());
        assertEquals("Juan", found.get().getName());
    }
}