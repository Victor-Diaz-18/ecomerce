package edu.unimagdalena.universitystore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {
    @NotBlank
    @Size(max = 50)
    // Unique only among records that are not deleted: see config/SoftDeleteUniqueIndexes
    @Column(nullable = false)
    private String sku;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    private String name;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean active;

    // Changes every time the photo changes (null: no photo); the photo itself lives in product_images
    @Column(name = "image_version")
    private Long imageVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
