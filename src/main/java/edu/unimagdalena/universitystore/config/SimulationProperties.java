package edu.unimagdalena.universitystore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ajustes del flujo automático de órdenes (prefijo "simulation" en application.properties).
 * Los plazos son días reales del calendario.
 *
 * @param enabled                  si es false, las órdenes no avanzan solas
 * @param originCity               ciudad desde la que despacha la tienda
 * @param originCountry            país desde el que despacha la tienda
 * @param daysToShip               días de preparación antes de enviar fuera de la ciudad de la tienda
 *                                 (dentro de ella se envía el mismo día del pago)
 * @param kmPerDay                 kilómetros que recorre un envío por día
 * @param transitDaysSameCity      días de viaje dentro de la ciudad de la tienda
 * @param transitDaysUnknownCity   días de viaje a una ciudad del país cuya ubicación no se conoce
 * @param transitDaysInternational días de viaje a otro país
 * @param earlyDeliveryChance      probabilidad (0 a 1) de que el paquete llegue un día antes de lo estimado
 * @param lateDeliveryChance       probabilidad (0 a 1) de que llegue uno o dos días después
 * @param daysToRequestReturn      días después de la entrega en que el cliente decide si pide devolución
 * @param returnRequestChance      probabilidad (0 a 1) de que el cliente pida la devolución
 * @param daysToResolveReturn      días que tarda la bodega en recibir y revisar el producto devuelto
 * @param returnAcceptChance       probabilidad (0 a 1) de que la devolución se acepte
 */
@ConfigurationProperties(prefix = "simulation")
public record SimulationProperties(
        boolean enabled,
        String originCity,
        String originCountry,
        int daysToShip,
        int kmPerDay,
        int transitDaysSameCity,
        int transitDaysUnknownCity,
        int transitDaysInternational,
        double earlyDeliveryChance,
        double lateDeliveryChance,
        int daysToRequestReturn,
        double returnRequestChance,
        int daysToResolveReturn,
        double returnAcceptChance
) {
}
