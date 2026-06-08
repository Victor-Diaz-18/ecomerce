package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.deletedAt = :now WHERE a.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Override
    @Query("SELECT a FROM Address a WHERE a.deletedAt IS NULL")
    List<Address> findAll();

    @Override
    @Query("SELECT a FROM Address a WHERE a.deletedAt IS NULL AND a.id = :id")
    Optional<Address> findById(Long id);
}