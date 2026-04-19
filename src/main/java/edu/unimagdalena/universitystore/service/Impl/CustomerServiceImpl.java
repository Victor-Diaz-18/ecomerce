package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    public Customer create(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);
        existing.setName(customer.getName());
        existing.setEmail(customer.getEmail());
        return customerRepository.save(existing);
    }
}