package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "customer.id", source = "customerId")
    Address toEntity(AddressDtos.CreateAddressRequest request);

    @Mapping(target = "customerId", source = "customer.id")
    AddressDtos.AddressResponse toResponse(Address address);
}