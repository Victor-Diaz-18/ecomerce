package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CustomerDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Customer toEntity(CustomerDtos.CreateCustomerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Customer toEntity(CustomerDtos.UpdateCustomerRequest request);

    CustomerDtos.CustomerResponse toResponse(Customer customer);
}