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
    void shouldMapCreateRequestToEntity() {
        AddressDtos.CreateAddressRequest request =
                new AddressDtos.CreateAddressRequest(
                        "Street 10",
                        "Santa Marta",
                        "Colombia",
                        1L
                );

        Address result = mapper.toEntity(request);

        assertEquals("Street 10", result.getStreet());
        assertEquals("Santa Marta", result.getCity());
        assertEquals(1L, result.getCustomer().getId());
    }

    @Test
    void shouldMapEntityToResponse() {
        Address address = Address.builder()
                .id(1L)
                .street("Street 10")
                .city("Santa Marta")
                .country("Colombia")
                .customer(Customer.builder().id(1L).build())
                .build();

        AddressDtos.AddressResponse result = mapper.toResponse(address);

        assertEquals(1L, result.customerId());
    }
}