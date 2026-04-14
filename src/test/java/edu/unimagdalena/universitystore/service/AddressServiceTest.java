package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.Impl.AddressServiceImpl;
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
class AddressServiceImplTest {
    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void shouldCreateAddress() {
        Customer customer = Customer.builder()
                .id(1L)
                .build();

        Address address = Address.builder()
                .street("Calle 10")
                .city("Santa Marta")
                .country("Colombia")
                .customer(customer)
                .build();

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(addressRepository.save(address)).thenReturn(address);

        Address result = addressService.create(address);

        assertNotNull(result);
        assertEquals("Calle 10", result.getStreet());
        verify(addressRepository).save(address);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        Customer customer = Customer.builder()
                .id(1L)
                .build();

        Address address = Address.builder()
                .customer(customer)
                .build();

        when(customerRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> addressService.create(address));

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void shouldFindAddressesByCustomer() {
        when(addressRepository.findByCustomerId(1L))
                .thenReturn(List.of(new Address()));

        List<Address> result = addressService.findByCustomer(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindAddressById() {
        Address address = new Address();
        address.setId(1L);

        when(addressRepository.findById(1L))
                .thenReturn(Optional.of(address));

        Address result = addressService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenAddressNotFound() {
        when(addressRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> addressService.findById(1L));

        assertEquals("Address not found", exception.getMessage());
    }
}