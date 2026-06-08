package edu.unimagdalena.universitystore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderItem extends BaseEntity {
    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false)
    @DecimalMin("0.01")
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @DecimalMin("0.01")
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}