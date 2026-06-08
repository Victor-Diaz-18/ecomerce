package edu.unimagdalena.universitystore.entity;

import edu.unimagdalena.universitystore.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusHistory extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private java.time.LocalDateTime changedAt;

    @Column(nullable = true)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;
}