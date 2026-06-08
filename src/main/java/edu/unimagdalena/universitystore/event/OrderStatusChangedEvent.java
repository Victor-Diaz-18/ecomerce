package edu.unimagdalena.universitystore.event;

import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.enums.OrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final PurchaseOrder order;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final LocalDateTime changedAt;

    public OrderStatusChangedEvent(Object source, PurchaseOrder order, OrderStatus oldStatus, OrderStatus newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = LocalDateTime.now();
    }
}