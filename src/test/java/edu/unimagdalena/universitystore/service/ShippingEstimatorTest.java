package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingEstimatorTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 12);

    @Mock
    private Random random;

    // store in the given city; 1 preparation day outside it, 350 km per day,
    // 20% chance of arriving early and 20% of arriving late
    private ShippingEstimator estimatorFrom(String originCity) {
        return new ShippingEstimator(new SimulationProperties(
                true, originCity, "Colombia", 1, 350, 1, 3, 8, 0.2, 0.2, 2, 0.2, 3, 0.7), random);
    }

    private ShippingEstimator estimator() {
        return estimatorFrom("Santa Marta");
    }

    private static Address address(String city, String country) {
        return Address.builder().street("Calle 1").city(city).country(country).build();
    }

    @Test
    void transitDaysGrowWithTheRealDistance() {
        // straight-line km from Santa Marta: Barranquilla ~70, Medellín ~570, Bogotá ~730, Pasto ~1170
        assertEquals(1, estimator().transitDays(address("Barranquilla", "Colombia")));
        assertEquals(2, estimator().transitDays(address("Medellín", "Colombia")));
        assertEquals(3, estimator().transitDays(address("Bogotá", "Colombia")));
        assertEquals(4, estimator().transitDays(address("Pasto", "Colombia")));
    }

    @Test
    void distanceBetweenSantaMartaAndBogotaIsAboutRight() {
        double km = ShippingEstimator.distanceKm(new double[]{11.24, -74.20}, new double[]{4.71, -74.07});

        assertEquals(726, km, 10);
    }

    @Test
    void sameCityShipsTheSameDayAndTravelsOneDay() {
        Address local = address(" santa marta ", "COLOMBIA");

        assertEquals(0, estimator().preparationDays(local));
        assertEquals(1, estimator().transitDays(local));
        assertEquals(DAY.plusDays(1), estimator().estimatedDelivery(DAY, local));
    }

    @Test
    void anotherCityWaitsThePreparationDayBeforeShipping() {
        Address bogota = address("Bogota", "Colombia");

        assertEquals(1, estimator().preparationDays(bogota));
        assertEquals(DAY.plusDays(4), estimator().estimatedDelivery(DAY, bogota));
    }

    @Test
    void theStoreCanBeInAnotherCity() {
        assertEquals(1, estimatorFrom("Bogotá").transitDays(address("bogota", "Colombia")));
        assertEquals(3, estimatorFrom("Bogotá").transitDays(address("Santa Marta", "Colombia")));
    }

    @Test
    void unknownCityAndForeignCountryUseFixedTimes() {
        assertEquals(3, estimator().transitDays(address("Aracataca", "Colombia")));
        assertEquals(8, estimator().transitDays(address("Caracas", "Venezuela")));
        assertEquals(3, estimator().transitDays(null));
    }

    @Test
    void packageUsuallyArrivesOnTheEstimatedDay() {
        when(random.nextDouble()).thenReturn(0.9);

        assertEquals(DAY.plusDays(3), estimator().actualArrival(DAY, address("Bogotá", "Colombia")));
    }

    @Test
    void packageCanArriveADayEarly() {
        when(random.nextDouble()).thenReturn(0.1);

        assertEquals(DAY.plusDays(2), estimator().actualArrival(DAY, address("Bogotá", "Colombia")));
    }

    @Test
    void packageCanArriveOneOrTwoDaysLate() {
        when(random.nextDouble()).thenReturn(0.3);
        when(random.nextInt(2)).thenReturn(0, 1);

        assertEquals(DAY.plusDays(4), estimator().actualArrival(DAY, address("Bogotá", "Colombia")));
        assertEquals(DAY.plusDays(5), estimator().actualArrival(DAY, address("Bogotá", "Colombia")));
    }

    @Test
    void packageNeverArrivesTheDayItShips() {
        when(random.nextDouble()).thenReturn(0.1);

        assertEquals(DAY.plusDays(1), estimator().actualArrival(DAY, address("Santa Marta", "Colombia")));
    }
}
