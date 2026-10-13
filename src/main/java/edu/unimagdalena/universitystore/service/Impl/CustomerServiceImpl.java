package edu.unimagdalena.universitystore.service.Impl;

import edu.unimagdalena.universitystore.entity.Customer;
import edu.unimagdalena.universitystore.enums.CustomerStatus;
import edu.unimagdalena.universitystore.exception.ConflictException;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.repository.CustomerRepository;
import edu.unimagdalena.universitystore.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer create(Customer customer) {
        if (customer.getEmail() != null && customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists");
        }
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer existing = findById(id);

        if (customer.getName() != null) {
            existing.setName(customer.getName());
        }
        if (customer.getEmail() != null && !customer.getEmail().equalsIgnoreCase(existing.getEmail())) {
            if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
                throw new ConflictException("Email already exists");
            }
            existing.setEmail(customer.getEmail());
        }
        if (customer.getStatus() != null) {
            existing.setStatus(customer.getStatus());
        }
        return customerRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.softDelete();
        customerRepository.save(customer);
    }
}
