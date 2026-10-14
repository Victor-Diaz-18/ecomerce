package edu.unimagdalena.universitystore.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Festivos de Colombia (Ley 51 de 1983, "Ley Emiliani"), calculados para cualquier año:
 * - fijos: se celebran en su fecha;
 * - trasladables: si no caen en lunes, pasan al lunes siguiente;
 * - de Semana Santa: dependen del Domingo de Pascua.
 */
public final class ColombianHolidays {
    private static final List<MonthDay> FIXED = List.of(
            MonthDay.of(1, 1),    // Año Nuevo
            MonthDay.of(5, 1),    // Día del Trabajo
            MonthDay.of(7, 20),   // Independencia
            MonthDay.of(8, 7),    // Batalla de Boyacá
            MonthDay.of(12, 8),   // Inmaculada Concepción
            MonthDay.of(12, 25)); // Navidad

    private static final List<MonthDay> MOVED_TO_MONDAY = List.of(
            MonthDay.of(1, 6),    // Reyes Magos
            MonthDay.of(3, 19),   // San José
            MonthDay.of(6, 29),   // San Pedro y San Pablo
            MonthDay.of(8, 15),   // Asunción de la Virgen
            MonthDay.of(10, 12),  // Día de la Raza
            MonthDay.of(11, 1),   // Todos los Santos
            MonthDay.of(11, 11)); // Independencia de Cartagena

    private static final Map<Integer, Set<LocalDate>> BY_YEAR = new ConcurrentHashMap<>();

    private ColombianHolidays() {
    }

    public static boolean isHoliday(LocalDate date) {
        return BY_YEAR.computeIfAbsent(date.getYear(), ColombianHolidays::holidaysOf).contains(date);
    }

    static Set<LocalDate> holidaysOf(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        FIXED.forEach(day -> holidays.add(day.atYear(year)));
        MOVED_TO_MONDAY.forEach(day -> holidays.add(nextMonday(day.atYear(year))));

        LocalDate easter = easterSunday(year);
        holidays.add(easter.minusDays(3));              // Jueves Santo
        holidays.add(easter.minusDays(2));              // Viernes Santo
        holidays.add(nextMonday(easter.plusDays(39)));  // Ascensión del Señor
        holidays.add(nextMonday(easter.plusDays(60)));  // Corpus Christi
        holidays.add(nextMonday(easter.plusDays(68)));  // Sagrado Corazón
        return holidays;
    }

    // A holiday that is not on a Monday is celebrated on the following Monday
    private static LocalDate nextMonday(LocalDate date) {
        LocalDate day = date;
        while (day.getDayOfWeek() != DayOfWeek.MONDAY) {
            day = day.plusDays(1);
        }
        return day;
    }

    // Anonymous Gregorian algorithm (Meeus/Jones/Butcher)
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }
}
