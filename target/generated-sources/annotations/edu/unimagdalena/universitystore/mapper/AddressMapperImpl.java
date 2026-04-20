package edu.unimagdalena.universitystore.mapper;

import edu.unimagdalena.universitystore.dto.AddressDtos;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-20T14:50:14-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public Address toEntity(AddressDtos.CreateAddressRequest request) {
        if ( request == null ) {
            return null;
        }

        Address.AddressBuilder address = Address.builder();

        address.customer( createAddressRequestToCustomer( request ) );
        address.street( request.street() );
        address.city( request.city() );
        address.country( request.country() );

        return address.build();
    }

    @Override
    public AddressDtos.AddressResponse toResponse(Address address) {
        if ( address == null ) {
            return null;
        }

        Long customerId = null;
        Long id = null;
        String street = null;
        String city = null;
        String country = null;

        customerId = addressCustomerId( address );
        id = address.getId();
        street = address.getStreet();
        city = address.getCity();
        country = address.getCountry();

        AddressDtos.AddressResponse addressResponse = new AddressDtos.AddressResponse( id, street, city, country, customerId );

        return addressResponse;
    }

    protected Customer createAddressRequestToCustomer(AddressDtos.CreateAddressRequest createAddressRequest) {
        if ( createAddressRequest == null ) {
            return null;
        }

        Customer.CustomerBuilder customer = Customer.builder();

        customer.id( createAddressRequest.customerId() );

        return customer.build();
    }

    private Long addressCustomerId(Address address) {
        Customer customer = address.getCustomer();
        if ( customer == null ) {
            return null;
        }
        return customer.getId();
    }
}
