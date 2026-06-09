package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T21:09:30-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressDtos.AddressResponse toResponse(Address address) {
        if ( address == null ) {
            return null;
        }

        Long customerId = null;
        String customerName = null;
        Long id = null;
        String street = null;
        String city = null;
        String country = null;

        customerId = addressCustomerId( address );
        customerName = addressCustomerName( address );
        id = address.getId();
        street = address.getStreet();
        city = address.getCity();
        country = address.getCountry();

        String addressLine = address.getStreet() != null && address.getCity() != null && address.getCountry() != null ? address.getStreet() + ", " + address.getCity() + ", " + address.getCountry() : null;

        AddressDtos.AddressResponse addressResponse = new AddressDtos.AddressResponse( id, street, city, country, customerId, customerName, addressLine );

        return addressResponse;
    }

    private Long addressCustomerId(Address address) {
        Customer customer = address.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }

    private String addressCustomerName(Address address) {
        Customer customer = address.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getName();
    }
}
