package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.Address;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT a FROM Address a WHERE a.deletedAt IS NULL AND a.customer.id = :customerId")
    List<Address> findByCustomerId(Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.deletedAt = :now WHERE a.id = :id")
    void softDelete(Long id, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Address a WHERE a.deletedAt IS NULL AND a.id = :id")
    boolean existsByIdNotDeleted(Long id);

    @Override
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT a FROM Address a WHERE a.deletedAt IS NULL")
    List<Address> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer"})
    @Query("SELECT a FROM Address a WHERE a.deletedAt IS NULL AND a.id = :id")
    Optional<Address> findById(Long id);
}
