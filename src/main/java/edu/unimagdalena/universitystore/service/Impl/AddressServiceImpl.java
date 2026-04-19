package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{
    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Address create(Address address) {
        if (!customerRepository.existsById(address.getCustomer().getId())) {
            throw new ResourceNotFoundException("Customer not found");
        }
        return addressRepository.save(address);
    }

    @Override
    public List<Address> findByCustomer(Long customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    @Override
    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    @Override
    public void delete(Long id) {
        addressRepository.deleteById(id);
    }
}