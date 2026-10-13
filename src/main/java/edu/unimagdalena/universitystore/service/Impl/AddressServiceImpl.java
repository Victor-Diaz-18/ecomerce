package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.service.ColombiaLocations;
import edu.unimagdalena.universitystore.service.ColombiaLocations.City;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Address create(Address address) {
        if (address.getCustomer() == null || address.getCustomer().getId() == null) {
            throw new ResourceNotFoundException("Customer not found");
        }
        Customer customer = customerRepository.findById(address.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        address.setCustomer(customer);

        // The city must be one the store ships to; its official spelling and department come from the list
        City city = ColombiaLocations.find(address.getCity())
                .orElseThrow(() -> new ValidationException("City is not in the list of shipping destinations"));
        address.setCity(city.name());
        address.setDepartment(city.department());
        address.setCountry(ColombiaLocations.COUNTRY);

        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByCustomer(Long customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.softDelete();
        addressRepository.save(address);
    }
}
