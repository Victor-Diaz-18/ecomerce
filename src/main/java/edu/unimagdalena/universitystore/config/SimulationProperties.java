package edu.unimagdalena.universitystore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ajustes del flujo automático de órdenes (prefijo "simulation" en application.properties).
 * Los plazos son días reales del calendario.
 *
 * @param enabled             si es false, las órdenes no avanzan solas
 * @param daysToShip          días que pasa una orden pagada antes de enviarse
 * @param daysToDeliver       días que tarda el envío en llegar
 * @param daysToRequestReturn días después de la entrega en que el cliente decide si pide devolución
 * @param returnRequestChance probabilidad (0 a 1) de que el cliente pida la devolución
 * @param daysToResolveReturn días que tarda la bodega en recibir y revisar el producto devuelto
 * @param returnAcceptChance  probabilidad (0 a 1) de que la devolución se acepte
 */
@ConfigurationProperties(prefix = "simulation")
public record SimulationProperties(
        boolean enabled,
        int daysToShip,
        int daysToDeliver,
        int daysToRequestReturn,
        double returnRequestChance,
        int daysToResolveReturn,
        double returnAcceptChance
) {
}
