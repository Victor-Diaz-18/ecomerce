package edu.unimagdalena.universitystore.event;

import edu.unimagdalena.universitystore.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderStatusChangedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedListener.class);

    private final OrderNotificationService notificationService;

    // After the change is saved, and on another thread, so a slow mail server never delays the customer.
    // The event only carries ids: the order is read again, since the original one is no longer attached.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Long orderId = event.getOrder().getId();
        log.info("Orden #{} cambió de estado: {} → {}", orderId, event.getOldStatus(), event.getNewStatus());
        try {
            notificationService.notifyStatusChange(orderId, event.getNewStatus());
        } catch (RuntimeException e) {
            log.warn("No se pudo preparar el correo de la orden #{}: {}", orderId, e.getMessage());
        }
    }
}
