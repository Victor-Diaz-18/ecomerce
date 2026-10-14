package edu.unimagdalena.universitystore.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class ColombianHolidaysTest {

    @Test
    void easterSundayOfKnownYears() {
        assertEquals(LocalDate.of(2025, 4, 20), ColombianHolidays.easterSunday(2025));
        assertEquals(LocalDate.of(2026, 4, 5), ColombianHolidays.easterSunday(2026));
        assertEquals(LocalDate.of(2027, 3, 28), ColombianHolidays.easterSunday(2027));
    }

    @Test
    void the18HolidaysOf2026() {
        Set<LocalDate> expected = Set.of(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 18), LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 7),
                LocalDate.of(2026, 8, 17), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25));

        assertEquals(new TreeSet<>(expected), new TreeSet<>(ColombianHolidays.holidaysOf(2026)));
    }

    @Test
    void ordinaryDaysAreNotHolidays() {
        assertFalse(ColombianHolidays.isHoliday(LocalDate.of(2026, 10, 13)));
        assertFalse(ColombianHolidays.isHoliday(LocalDate.of(2026, 1, 6)));  // Reyes moved to the 12th
        assertTrue(ColombianHolidays.isHoliday(LocalDate.of(2026, 1, 12)));
    }
}
