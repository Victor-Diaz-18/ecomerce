package edu.unimagdalena.universitystore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends BaseEntity {
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Email
    @Size(max = 100)
    // Unique only among records that are not deleted: see config/SoftDeleteUniqueIndexes
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private edu.unimagdalena.universitystore.enums.CustomerStatus status;

    // The account is saved and deleted on its own (AuthService, CustomerService); no cascade from here
    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private User user;
}
