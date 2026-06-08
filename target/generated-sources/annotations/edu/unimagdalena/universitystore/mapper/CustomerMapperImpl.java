package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.CustomerDtos;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T00:27:21-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerDtos.CustomerResponse toResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String email = null;
        CustomerStatus status = null;

        id = customer.getId();
        name = customer.getName();
        email = customer.getEmail();
        status = customer.getStatus();

        CustomerDtos.CustomerResponse customerResponse = new CustomerDtos.CustomerResponse( id, name, email, status );

        return customerResponse;
    }
}
