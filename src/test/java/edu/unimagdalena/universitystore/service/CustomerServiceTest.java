package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.Impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldCreateCustomer() {
        Customer customer = Customer.builder()
                .name("Juan Perez")
                .email("juan@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.empty());

        when(customerRepository.save(customer))
                .thenReturn(customer);

        Customer result = customerService.create(customer);

        assertNotNull(result);
        assertEquals("juan@example.com", result.getEmail());
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        Customer customer = Customer.builder()
                .name("Juan Perez")
                .email("juan@example.com")
                .build();

        when(customerRepository.findByEmail(customer.getEmail()))
                .thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.create(customer));

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void shouldFindAllCustomers() {
        when(customerRepository.findAll())
                .thenReturn(List.of(new Customer(), new Customer()));

        List<Customer> result = customerService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Maria")
                .build();

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        Customer result = customerService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Maria", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> customerService.findById(1L));

        assertEquals("Customer not found", exception.getMessage());
    }
}