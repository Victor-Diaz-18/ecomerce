package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.service.ColombiaLocations.City;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Calcula los plazos de un envío como lo hacen las transportadoras: por zonas (urbano, regional,
 * nacional, difícil acceso), en días hábiles y con hora de corte. La zona sale de la distancia real
 * entre la tienda y la ciudad de destino.
 */
@Component
@RequiredArgsConstructor
public class ShippingEstimator {
    private static final double EARTH_RADIUS_KM = 6371;

    private final SimulationProperties properties;
    private final Random random;

    /** Solo se hacen envíos dentro del país de la tienda. */
    public boolean deliversTo(Address address) {
        return address != null && sameText(address.getCountry(), properties.originCountry());
    }

    /** Las transportadoras no recogen ni entregan los domingos. */
    public boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    /** Día en que sale la orden: el del pago si fue antes de la hora de corte; si no, el siguiente día hábil. */
    public LocalDate shipDate(LocalDateTime paidAt) {
        LocalDate date = paidAt.toLocalDate();
        if (paidAt.getHour() >= properties.cutoffHour()) {
            date = date.plusDays(1);
        }
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    /** Días hábiles de viaje previstos desde que la orden sale de la tienda. */
    public int transitDays(Address address) {
        if (address == null) {
            return properties.transitDaysNational();
        }
        if (isOriginCity(address)) {
            return properties.transitDaysSameCity();
        }
        Optional<City> origin = ColombiaLocations.find(properties.originCity());
        Optional<City> destination = ColombiaLocations.find(address.getCity());
        // A city we cannot place on the map is treated as a regular national shipment
        if (origin.isEmpty() || destination.isEmpty()) {
            return properties.transitDaysNational();
        }
        if (destination.get().remote()) {
            return properties.transitDaysRemote();
        }
        return distanceKm(origin.get(), destination.get()) <= properties.regionalMaxKm()
                ? properties.transitDaysRegional()
                : properties.transitDaysNational();
    }

    /** La fecha que se le promete al cliente: el plazo máximo de la zona contado desde el día de salida. */
    public LocalDate estimatedDelivery(LocalDate shipDate, Address address) {
        return addBusinessDays(shipDate, transitDays(address));
    }

    /**
     * La fecha en que el paquete llega de verdad, decidida al despacharlo: lo normal es que tarde
     * lo previsto, pero a veces se adelanta un día o se atrasa uno o dos. Nunca llega el mismo día que sale.
     */
    public LocalDate actualArrival(LocalDate shippedOn, Address address) {
        int days = transitDays(address);
        double spin = random.nextDouble();
        if (spin < properties.earlyDeliveryChance()) {
            days -= 1;
        } else if (spin < properties.earlyDeliveryChance() + properties.lateDeliveryChance()) {
            days += 1 + random.nextInt(2);
        }
        return addBusinessDays(shippedOn, Math.max(1, days));
    }

    private LocalDate addBusinessDays(LocalDate from, int days) {
        LocalDate date = from;
        for (int added = 0; added < days; ) {
            date = date.plusDays(1);
            if (isBusinessDay(date)) {
                added++;
            }
        }
        return date;
    }

    private static double distanceKm(City from, City to) {
        return distanceKm(new double[]{from.latitude(), from.longitude()}, new double[]{to.latitude(), to.longitude()});
    }

    // Straight-line (great-circle) distance between two points given as {latitude, longitude}
    static double distanceKm(double[] from, double[] to) {
        double dLat = Math.toRadians(to[0] - from[0]);
        double dLon = Math.toRadians(to[1] - from[1]);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(from[0])) * Math.cos(Math.toRadians(to[0]))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    private boolean isOriginCity(Address address) {
        return deliversTo(address)
                && sameText(address.getCity(), properties.originCity());
    }

    private static boolean sameText(String a, String b) {
        return a != null && b != null && ColombiaLocations.normalize(a).equals(ColombiaLocations.normalize(b));
    }
}
