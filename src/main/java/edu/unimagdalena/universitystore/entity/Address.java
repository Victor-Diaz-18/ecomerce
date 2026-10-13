package edu.unimagdalena.universitystore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Address extends BaseEntity {
    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    private String street;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    // Barrio y departamento: las direcciones anteriores al formulario por partes no los tienen
    @Size(max = 100)
    private String neighborhood;

    @Size(max = 100)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** La dirección en una línea, con las partes que tenga: "Calle 13 # 3-84, Centro, Santa Marta, Magdalena, Colombia". */
    public String toLine() {
        return Stream.of(street, neighborhood, city, department, country)
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
    }
}
