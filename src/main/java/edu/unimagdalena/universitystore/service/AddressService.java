package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Address;

import java.util.List;

public interface AddressService {
    Address create(Address address);
    List<Address> findByCustomer(Long customerId);
    Address findById(Long id);
    void delete(Long id);
}