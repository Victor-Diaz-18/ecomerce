package edu.unimagdalena.universitystore.repository;

import edu.unimagdalena.universitystore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
         SELECT u FROM User u
         LEFT JOIN u.customer c WHERE c.email = :email
    """)
    Optional<User> findByCustomerEmail(@Param("email") String email);

    @Query("""
        SELECT COUNT(u) > 0 FROM User u 
        LEFT JOIN u.customer c WHERE c.email = :email
    """)
    boolean existsByCustomerEmail(@Param("email") String email);

}