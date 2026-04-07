package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Customer;

import java.util.List;

public interface CustomerService {
    Customer create(Customer customer);
    List<Customer> findAll();
    Customer findById(Long id);
}