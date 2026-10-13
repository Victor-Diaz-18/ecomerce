package edu.unimagdalena.universitystore.entity;

import edu.unimagdalena.universitystore.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PurchaseOrder extends BaseEntity {
    @NotNull
    @Column(nullable = false)
    private BigDecimal total;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Day the order entered its current status; the automatic flow counts its deadlines from here
    @Column(name = "status_changed_on")
    private LocalDate statusChangedOn;

    // Day the order leaves the store: payment day, or the next working day if paid after the cut-off hour
    @Column(name = "ship_date")
    private LocalDate shipDate;

    // Date promised to the customer at payment, worked out from how far the shipping address is
    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    // Day the package will really arrive (may be earlier or later than promised); decided when it ships
    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    // Day it was actually delivered
    @Column(name = "delivered_on")
    private LocalDate deliveredOn;

    // The automatic flow decides only once per order whether the customer asks for a return
    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "return_spin_done", nullable = false)
    private boolean returnSpinDone = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
}
