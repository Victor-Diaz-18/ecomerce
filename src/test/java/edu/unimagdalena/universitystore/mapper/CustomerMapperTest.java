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
    void shouldMapEntityToResponse() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Juan Perez")
                .email("juan@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        CustomerDtos.CustomerResponse result = mapper.toResponse(customer);

        assertEquals(1L, result.id());
        assertEquals("Juan Perez", result.name());
        assertEquals("juan@example.com", result.email());
        assertEquals(CustomerStatus.ACTIVE, result.status());
    }
}
