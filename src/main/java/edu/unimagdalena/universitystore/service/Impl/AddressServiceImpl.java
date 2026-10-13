package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.service.ColombiaLocations;
import edu.unimagdalena.universitystore.service.StreetFormat;
import edu.unimagdalena.universitystore.service.ColombiaLocations.City;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.AddressService;
import edu.unimagdalena.universitystore.service.PurchaseOrderService;
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
    private final PurchaseOrderService purchaseOrderService;

    @Override
    @Transactional
    public Address create(Address address) {
        if (address.getCustomer() == null || address.getCustomer().getId() == null) {
            throw new ResourceNotFoundException("Customer not found");
        }
        Customer customer = customerRepository.findById(address.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        address.setCustomer(customer);
        address.setStreet(StreetFormat.normalize(address.getStreet()));

        applyCity(address, address.getCity());

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address update(Long id, String street, String neighborhood, String city) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        address.setStreet(StreetFormat.normalize(street));
        address.setNeighborhood(neighborhood);
        applyCity(address, city);

        Address saved = addressRepository.save(address);
        purchaseOrderService.refreshEstimatedDelivery(saved.getId());
        return saved;
    }

    // The city must be one the store ships to; its official spelling and department come from the list
    private void applyCity(Address address, String cityName) {
        City city = ColombiaLocations.find(cityName)
                .orElseThrow(() -> new ValidationException("City is not in the list of shipping destinations"));
        address.setCity(city.name());
        address.setDepartment(city.department());
        address.setCountry(ColombiaLocations.COUNTRY);
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
