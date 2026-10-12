package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.config.SimulationProperties;
import edu.unimagdalena.universitystore.entity.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingEstimatorTest {
    // store in Santa Marta, Colombia: 1 day to prepare, then 1 / 3 / 8 days of travel
    private final ShippingEstimator estimator = new ShippingEstimator(
            new SimulationProperties(true, 1, "Santa Marta", "Colombia", 1, 3, 8, 2, 0.2, 3, 0.7));

    private static Address address(String city, String country) {
        return Address.builder().street("Calle 1").city(city).country(country).build();
    }

    @Test
    void sameCityTravelsOneDay() {
        assertEquals(1, estimator.transitDays(address("Santa Marta", "Colombia")));
    }

    @Test
    void anotherCityInTheSameCountryTravelsThreeDays() {
        assertEquals(3, estimator.transitDays(address("Bogotá", "Colombia")));
    }

    @Test
    void anotherCountryTravelsEightDays() {
        assertEquals(8, estimator.transitDays(address("Santa Marta", "Venezuela")));
    }

    @Test
    void ignoresCaseSpacesAndAccents() {
        SimulationProperties bogota = new SimulationProperties(true, 1, "Bogotá", "Colombia", 1, 3, 8, 2, 0.2, 3, 0.7);

        assertEquals(1, new ShippingEstimator(bogota).transitDays(address(" bogota ", "COLOMBIA")));
    }

    @Test
    void missingAddressUsesTheSameCountryTime() {
        assertEquals(3, estimator.transitDays(null));
    }

    @Test
    void deliveryPromiseAtPaymentAddsThePreparationDay() {
        assertEquals(4, estimator.daysFromPaymentToDelivery(address("Medellín", "Colombia")));
    }
}
