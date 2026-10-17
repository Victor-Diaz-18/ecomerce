package edu.unimagdalena.universitystore.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * La foto de un producto, guardada en la base de datos (una por producto). Va en una tabla aparte
 * para que leer la lista de productos no cargue también los bytes de cada imagen.
 */
@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false)
    private byte[] data;
}
