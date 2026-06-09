package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "addressLine", expression = "java(address.getStreet() != null && address.getCity() != null && address.getCountry() != null ? address.getStreet() + \", \" + address.getCity() + \", \" + address.getCountry() : null)")
    AddressDtos.AddressResponse toResponse(Address address);
}
