package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.deletedAt = :now WHERE c.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT c FROM Customer c WHERE c.deletedAt IS NULL")
    List<Customer> findAll();

    @Override
    @Query("SELECT c FROM Customer c WHERE c.deletedAt IS NULL AND c.id = :id")
    Optional<Customer> findById(Long id);
}