package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CustomerDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDtos.CustomerResponse toResponse(Customer customer);
}