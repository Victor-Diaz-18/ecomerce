package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Calcula los plazos de un envío como lo hacen las transportadoras: por zonas (urbano, regional,
 * nacional, difícil acceso), en días hábiles y con hora de corte. La zona sale de la distancia real
 * entre la tienda y la ciudad de destino.
 */
@Component
@RequiredArgsConstructor
public class ShippingEstimator {
    private static final double EARTH_RADIUS_KM = 6371;

    // Latitud y longitud de las principales ciudades de Colombia (clave sin tildes y en minúsculas)
    private static final Map<String, double[]> CITY_COORDINATES = Map.ofEntries(
            Map.entry("santa marta", new double[]{11.24, -74.20}),
            Map.entry("cienaga", new double[]{11.01, -74.25}),
            Map.entry("barranquilla", new double[]{10.97, -74.80}),
            Map.entry("soledad", new double[]{10.92, -74.77}),
            Map.entry("cartagena", new double[]{10.39, -75.48}),
            Map.entry("riohacha", new double[]{11.54, -72.91}),
            Map.entry("valledupar", new double[]{10.46, -73.25}),
            Map.entry("sincelejo", new double[]{9.30, -75.40}),
            Map.entry("monteria", new double[]{8.75, -75.88}),
            Map.entry("cucuta", new double[]{7.89, -72.50}),
            Map.entry("bucaramanga", new double[]{7.12, -73.12}),
            Map.entry("arauca", new double[]{7.08, -70.76}),
            Map.entry("medellin", new double[]{6.25, -75.56}),
            Map.entry("quibdo", new double[]{5.69, -76.66}),
            Map.entry("tunja", new double[]{5.54, -73.36}),
            Map.entry("yopal", new double[]{5.34, -72.40}),
            Map.entry("manizales", new double[]{5.07, -75.52}),
            Map.entry("pereira", new double[]{4.81, -75.69}),
            Map.entry("bogota", new double[]{4.71, -74.07}),
            Map.entry("armenia", new double[]{4.53, -75.68}),
            Map.entry("ibague", new double[]{4.44, -75.23}),
            Map.entry("villavicencio", new double[]{4.14, -73.63}),
            Map.entry("cali", new double[]{3.45, -76.53}),
            Map.entry("neiva", new double[]{2.93, -75.28}),
            Map.entry("popayan", new double[]{2.44, -76.61}),
            Map.entry("florencia", new double[]{1.61, -75.61}),
            Map.entry("pasto", new double[]{1.21, -77.28}),
            Map.entry("leticia", new double[]{-4.21, -69.94}),
            Map.entry("san andres", new double[]{12.58, -81.70})
    );

    // Destinos a los que solo se llega por avión o río
    private static final Set<String> REMOTE_CITIES = Set.of("leticia", "san andres", "quibdo");

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
        if (REMOTE_CITIES.contains(normalize(address.getCity()))) {
            return properties.transitDaysRemote();
        }
        double[] origin = CITY_COORDINATES.get(normalize(properties.originCity()));
        double[] destination = CITY_COORDINATES.get(normalize(address.getCity()));
        // A city we cannot place on the map is treated as a regular national shipment
        if (origin == null || destination == null) {
            return properties.transitDaysNational();
        }
        return distanceKm(origin, destination) <= properties.regionalMaxKm()
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
