package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AddressRepositoryTest {
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindAddressesByCustomerId() {
        Customer customer = customerRepository.save(
                Customer.builder()
                        .name("Maria")
                        .email("maria@test.com")
                        .status(CustomerStatus.ACTIVE)
                        .build()
        );

        addressRepository.save(
                Address.builder()
                        .street("Street 1")
                        .city("Santa Marta")
                        .country("Colombia")
                        .customer(customer)
                        .build()
        );

        List<Address> result = addressRepository.findByCustomerId(customer.getId());

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}