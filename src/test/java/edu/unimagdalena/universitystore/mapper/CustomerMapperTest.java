package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CustomerDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {
    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void shouldMapCreateRequestToEntity() {
        CustomerDtos.CreateCustomerRequest request =
                new CustomerDtos.CreateCustomerRequest(
                        "Juan Perez",
                        "juan@test.com"
                );

        Customer result = mapper.toEntity(request);

        assertEquals("Juan Perez", result.getName());
        assertEquals("juan@test.com", result.getEmail());
    }

    @Test
    void shouldMapEntityToResponse() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@test.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse result = mapper.toResponse(customer);

        assertEquals("Juan Perez", result.name());
        assertEquals("juan@test.com", result.email());
        assertEquals(CustomerStatus.ACTIVE, result.status());
    }
}