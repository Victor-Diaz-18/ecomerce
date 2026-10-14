package edu.unimagdalena.universitystore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ajustes del flujo automático de órdenes (prefijo "simulation" en application.properties).
 * Los plazos de envío son días hábiles (de lunes a sábado); los de devolución, días del calendario.
 *
 * @param enabled                  si es false, las órdenes no avanzan solas
 * @param unpaidOrderHours         horas que tiene el cliente para pagar una orden; después se cancela sola
 * @param originCity               ciudad desde la que despacha la tienda
 * @param originCountry            país desde el que despacha la tienda (solo se envía dentro de él)
 * @param cutoffHour               hora de corte (0 a 23): lo pagado antes sale ese día; lo demás, el siguiente día hábil
 * @param regionalMaxKm            distancia máxima, en km, para que un destino cuente como regional
 * @param transitDaysSameCity      días de viaje dentro de la ciudad de la tienda (urbano)
 * @param transitDaysRegional      días de viaje a una ciudad cercana (regional)
 * @param transitDaysNational      días de viaje al resto del país (nacional)
 * @param transitDaysRemote        días de viaje a destinos de difícil acceso
 * @param earlyDeliveryChance      probabilidad (0 a 1) de que el paquete llegue un día antes de lo estimado
 * @param lateDeliveryChance       probabilidad (0 a 1) de que llegue uno o dos días después
 * @param daysToResolveReturn      días que tarda la bodega en recibir y revisar el producto devuelto
 * @param returnAcceptChance       probabilidad (0 a 1) de que la devolución se acepte
 */
@ConfigurationProperties(prefix = "simulation")
public record SimulationProperties(
        boolean enabled,
        int unpaidOrderHours,
        String originCity,
        String originCountry,
        int cutoffHour,
        int regionalMaxKm,
        int transitDaysSameCity,
        int transitDaysRegional,
        int transitDaysNational,
        int transitDaysRemote,
        double earlyDeliveryChance,
        double lateDeliveryChance,
        int daysToResolveReturn,
        double returnAcceptChance
) {
}
