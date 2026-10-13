package edu.unimagdalena.universitystore.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedListener.class);

    @Async
    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Orden #{} cambió de estado: {} → {} (cliente: {})",
                event.getOrder().getId(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getOrder().getCustomer() != null ? event.getOrder().getCustomer().getName() : "N/A");
        // Aquí se podría enviar email, notificación push, webhook, etc.
    }
}