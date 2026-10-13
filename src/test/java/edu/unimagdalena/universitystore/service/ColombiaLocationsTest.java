package edu.unimagdalena.universitystore.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ColombiaLocationsTest {

    @Test
    void findsCityIgnoringCaseSpacesAndAccents() {
        assertEquals("Bogotá", ColombiaLocations.find(" BOGOTA ").orElseThrow().name());
        assertEquals("Magdalena", ColombiaLocations.find("santa marta").orElseThrow().department());
        assertTrue(ColombiaLocations.find("Macondo").isEmpty());
        assertTrue(ColombiaLocations.find(null).isEmpty());
    }

    @Test
    void coversThe32DepartmentsAndTheCapitalDistrict() {
        Map<String, List<String>> departments = ColombiaLocations.citiesByDepartment();

        assertEquals(33, departments.size());
        assertTrue(departments.get("Magdalena").contains("Santa Marta"));
        assertTrue(departments.values().stream().noneMatch(List::isEmpty));
    }

    @Test
    void everyCityIsInsideColombia() {
        ColombiaLocations.citiesByDepartment().values().stream().flatMap(List::stream).forEach(name -> {
            var city = ColombiaLocations.find(name).orElseThrow();
            assertTrue(city.latitude() > -4.5 && city.latitude() < 13.5, name);
            assertTrue(city.longitude() > -82 && city.longitude() < -66.5, name);
        });
    }
}
