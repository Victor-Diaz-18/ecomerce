package edu.unimagdalena.universitystore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Reglas de la tienda para los clientes (prefijo "store" en application.properties).
 *
 * @param returnDays días, contados desde la entrega, en los que el cliente puede pedir una devolución
 */
@ConfigurationProperties(prefix = "store")
public record StorePolicy(int returnDays) {
}
