package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingEstimatorTest {
    // 12 Oct 2026 is a Monday, so the 17th is a Saturday and the 18th a Sunday
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 12);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 10, 17);

    @Mock
    private Random random;

    // store in the given city; cut-off at 14:00; regional up to 300 km;
    // 1 / 2 / 3 / 6 working days for urban / regional / national / remote;
    // 20% chance of arriving early and 20% of arriving late
    private ShippingEstimator estimatorFrom(String originCity) {
        return new ShippingEstimator(new SimulationProperties(
                true, originCity, "Colombia", 14, 300, 1, 2, 3, 6, 0.2, 0.2, 2, 0.2, 3, 0.7), random);
    }

    private ShippingEstimator estimator() {
        return estimatorFrom("Santa Marta");
    }

    private static Address address(String city, String country) {
        return Address.builder().street("Calle 1").city(city).country(country).build();
    }

    @Test
    void transitDaysDependOnTheZoneOfTheDestination() {
        assertEquals(1, estimator().transitDays(address(" santa marta ", "COLOMBIA")));
        // straight-line km from Santa Marta: Barranquilla ~70, Cartagena ~170, Sincelejo ~250
        assertEquals(2, estimator().transitDays(address("Barranquilla", "Colombia")));
        assertEquals(2, estimator().transitDays(address("Cartagena", "Colombia")));
        assertEquals(2, estimator().transitDays(address("Sincelejo", "Colombia")));
        // Medellín ~575, Bogotá ~725, Pasto ~1165
        assertEquals(3, estimator().transitDays(address("Medellín", "Colombia")));
        assertEquals(3, estimator().transitDays(address("Bogotá", "Colombia")));
        assertEquals(3, estimator().transitDays(address("Pasto", "Colombia")));
        assertEquals(6, estimator().transitDays(address("Leticia", "Colombia")));
    }

    @Test
    void onlyDeliversInsideTheCountryOfTheStore() {
        assertTrue(estimator().deliversTo(address("Pasto", " colombia")));
        assertFalse(estimator().deliversTo(address("Caracas", "Venezuela")));
        assertFalse(estimator().deliversTo(null));
    }

    @Test
    void distanceBetweenSantaMartaAndBogotaIsAboutRight() {
        double km = ShippingEstimator.distanceKm(new double[]{11.24, -74.20}, new double[]{4.71, -74.07});

        assertEquals(726, km, 10);
    }

    @Test
    void theStoreCanBeInAnotherCity() {
        assertEquals(1, estimatorFrom("Bogotá").transitDays(address("bogota", "Colombia")));
        assertEquals(2, estimatorFrom("Bogotá").transitDays(address("Tunja", "Colombia")));
        assertEquals(3, estimatorFrom("Bogotá").transitDays(address("Santa Marta", "Colombia")));
    }

    @Test
    void cityThatIsNotOnTheMapCountsAsNational() {
        assertEquals(3, estimator().transitDays(address("Macondo", "Colombia")));
        assertEquals(3, estimator().transitDays(null));
    }

    @Test
    void paidBeforeTheCutOffShipsTheSameDay() {
        assertEquals(MONDAY, estimator().shipDate(MONDAY.atTime(13, 59)));
    }

    @Test
    void paidAfterTheCutOffShipsTheNextDay() {
        assertEquals(MONDAY.plusDays(1), estimator().shipDate(MONDAY.atTime(14, 0)));
    }

    @Test
    void nothingShipsOnSunday() {
        assertEquals(MONDAY.plusDays(7), estimator().shipDate(SATURDAY.atTime(18, 0)));
        assertEquals(MONDAY.plusDays(7), estimator().shipDate(SATURDAY.plusDays(1).atTime(9, 0)));
        assertTrue(estimator().isBusinessDay(SATURDAY));
        assertFalse(estimator().isBusinessDay(SATURDAY.plusDays(1)));
    }

    @Test
    void estimateCountsWorkingDaysFromTheShipDate() {
        Address bogota = address("Bogota", "Colombia");

        assertEquals(MONDAY.plusDays(3), estimator().estimatedDelivery(MONDAY, bogota));
        // Friday + 3 working days = Saturday, Monday, Tuesday
        assertEquals(MONDAY.plusDays(8), estimator().estimatedDelivery(MONDAY.plusDays(4), bogota));
    }

    @Test
    void packageUsuallyArrivesOnTheEstimatedDay() {
        when(random.nextDouble()).thenReturn(0.9);

        assertEquals(MONDAY.plusDays(3), estimator().actualArrival(MONDAY, address("Bogotá", "Colombia")));
    }

    @Test
    void packageCanArriveADayEarly() {
        when(random.nextDouble()).thenReturn(0.1);

        assertEquals(MONDAY.plusDays(1), estimator().actualArrival(MONDAY, address("Barranquilla", "Colombia")));
    }

    @Test
    void packageCanArriveOneOrTwoDaysLate() {
        when(random.nextDouble()).thenReturn(0.3);
        when(random.nextInt(2)).thenReturn(0, 1);

        assertEquals(MONDAY.plusDays(4), estimator().actualArrival(MONDAY, address("Bogotá", "Colombia")));
        assertEquals(MONDAY.plusDays(5), estimator().actualArrival(MONDAY, address("Bogotá", "Colombia")));
    }

    @Test
    void packageNeverArrivesTheDayItShipsNorOnSunday() {
        when(random.nextDouble()).thenReturn(0.1);

        assertEquals(MONDAY.plusDays(1), estimator().actualArrival(MONDAY, address("Santa Marta", "Colombia")));
        assertEquals(MONDAY.plusDays(7), estimator().actualArrival(SATURDAY, address("Santa Marta", "Colombia")));
    }
}
