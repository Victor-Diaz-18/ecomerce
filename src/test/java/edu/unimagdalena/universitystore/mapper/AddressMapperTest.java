package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {
    private final AddressMapper mapper = Mappers.getMapper(AddressMapper.class);

    @Test
    void shouldMapEntityToResponse() {
        Address address = Address.builder()
                .id(1L)
                .street("Calle 10")
                .city("Santa Marta")
                .country("Colombia")
                .customer(Customer.builder().id(2L).name("Juan").build())
                .build();

        AddressDtos.AddressResponse result = mapper.toResponse(address);

        assertEquals(1L, result.id());
        assertEquals("Calle 10", result.street());
        assertEquals(2L, result.customerId());
        assertEquals("Juan", result.customerName());
        assertEquals("Calle 10, Santa Marta, Colombia", result.addressLine());
    }

    @Test
    void shouldLeaveAddressLineNullWhenAPartIsMissing() {
        Address address = Address.builder().id(1L).street("Calle 10").city("Santa Marta").build();

        AddressDtos.AddressResponse result = mapper.toResponse(address);

        assertNull(result.addressLine());
        assertNull(result.customerId());
    }
}
