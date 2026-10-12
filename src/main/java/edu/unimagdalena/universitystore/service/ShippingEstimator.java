package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

/**
 * Calcula cuántos días tarda un envío según qué tan lejos queda la dirección de la tienda:
 * misma ciudad, otra ciudad del mismo país u otro país.
 */
@Component
@RequiredArgsConstructor
public class ShippingEstimator {
    private final SimulationProperties properties;

    /** Días de viaje desde que la orden sale de la tienda. */
    public int transitDays(Address address) {
        if (address == null) {
            return properties.transitDaysSameCountry();
        }
        if (!sameText(address.getCountry(), properties.originCountry())) {
            return properties.transitDaysInternational();
        }
        return sameText(address.getCity(), properties.originCity())
                ? properties.transitDaysSameCity()
                : properties.transitDaysSameCountry();
    }

    /** Días desde el pago hasta la entrega: preparación en la tienda más el viaje. */
    public int daysFromPaymentToDelivery(Address address) {
        return properties.daysToShip() + transitDays(address);
    }

    // "Santa Marta", "santa marta " and "SANTA MARTA" are the same place; so are "Bogotá" and "Bogota"
    private static boolean sameText(String a, String b) {
        return a != null && b != null && normalize(a).equals(normalize(b));
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }
}
