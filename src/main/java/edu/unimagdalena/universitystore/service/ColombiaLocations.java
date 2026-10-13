package edu.unimagdalena.universitystore.service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ciudades de Colombia a las que la tienda envía, con su departamento y coordenadas aproximadas.
 * Es la única lista: de aquí salen los menús del formulario de direcciones y las distancias de envío.
 */
public final class ColombiaLocations {
    public static final String COUNTRY = "Colombia";

    /**
     * @param remote destino de difícil acceso, al que se llega por avión, río o vías precarias
     */
    public record City(String name, String department, double latitude, double longitude, boolean remote) {
    }

    private static final List<City> CITIES = List.of(
            remote("Leticia", "Amazonas", -4.21, -69.94),
            city("Medellín", "Antioquia", 6.25, -75.56),
            city("Bello", "Antioquia", 6.34, -75.56),
            city("Envigado", "Antioquia", 6.17, -75.58),
            city("Itagüí", "Antioquia", 6.17, -75.61),
            city("Rionegro", "Antioquia", 6.15, -75.37),
            city("Apartadó", "Antioquia", 7.88, -76.63),
            city("Arauca", "Arauca", 7.08, -70.76),
            city("Barranquilla", "Atlántico", 10.97, -74.80),
            city("Soledad", "Atlántico", 10.92, -74.77),
            city("Malambo", "Atlántico", 10.86, -74.77),
            city("Puerto Colombia", "Atlántico", 10.99, -74.95),
            city("Bogotá", "Bogotá D.C.", 4.71, -74.07),
            city("Cartagena", "Bolívar", 10.39, -75.48),
            city("Turbaco", "Bolívar", 10.33, -75.41),
            city("Magangué", "Bolívar", 9.24, -74.75),
            city("Tunja", "Boyacá", 5.54, -73.36),
            city("Duitama", "Boyacá", 5.83, -73.03),
            city("Sogamoso", "Boyacá", 5.72, -72.93),
            city("Manizales", "Caldas", 5.07, -75.52),
            city("Florencia", "Caquetá", 1.61, -75.61),
            city("Yopal", "Casanare", 5.34, -72.40),
            city("Popayán", "Cauca", 2.44, -76.61),
            city("Valledupar", "Cesar", 10.46, -73.25),
            city("Aguachica", "Cesar", 8.31, -73.62),
            remote("Quibdó", "Chocó", 5.69, -76.66),
            city("Montería", "Córdoba", 8.75, -75.88),
            city("Soacha", "Cundinamarca", 4.58, -74.22),
            city("Chía", "Cundinamarca", 4.86, -74.03),
            city("Zipaquirá", "Cundinamarca", 5.02, -74.00),
            city("Facatativá", "Cundinamarca", 4.81, -74.35),
            city("Girardot", "Cundinamarca", 4.30, -74.80),
            remote("Inírida", "Guainía", 3.87, -67.92),
            city("San José del Guaviare", "Guaviare", 2.57, -72.64),
            city("Neiva", "Huila", 2.93, -75.28),
            city("Pitalito", "Huila", 1.85, -76.05),
            city("Riohacha", "La Guajira", 11.54, -72.91),
            city("Maicao", "La Guajira", 11.38, -72.24),
            city("Santa Marta", "Magdalena", 11.24, -74.20),
            city("Ciénaga", "Magdalena", 11.01, -74.25),
            city("Aracataca", "Magdalena", 10.59, -74.19),
            city("Fundación", "Magdalena", 10.52, -74.18),
            city("El Banco", "Magdalena", 9.00, -73.97),
            city("Villavicencio", "Meta", 4.14, -73.63),
            city("Pasto", "Nariño", 1.21, -77.28),
            city("Ipiales", "Nariño", 0.83, -77.64),
            city("Tumaco", "Nariño", 1.80, -78.76),
            city("Cúcuta", "Norte de Santander", 7.89, -72.50),
            city("Ocaña", "Norte de Santander", 8.24, -73.36),
            city("Pamplona", "Norte de Santander", 7.38, -72.65),
            city("Mocoa", "Putumayo", 1.15, -76.65),
            city("Armenia", "Quindío", 4.53, -75.68),
            city("Pereira", "Risaralda", 4.81, -75.69),
            city("Dosquebradas", "Risaralda", 4.84, -75.67),
            remote("San Andrés", "San Andrés y Providencia", 12.58, -81.70),
            city("Bucaramanga", "Santander", 7.12, -73.12),
            city("Floridablanca", "Santander", 7.06, -73.09),
            city("Girón", "Santander", 7.07, -73.17),
            city("Piedecuesta", "Santander", 6.99, -73.05),
            city("Barrancabermeja", "Santander", 7.06, -73.85),
            city("Sincelejo", "Sucre", 9.30, -75.40),
            city("Ibagué", "Tolima", 4.44, -75.23),
            city("Espinal", "Tolima", 4.15, -74.88),
            city("Cali", "Valle del Cauca", 3.45, -76.53),
            city("Palmira", "Valle del Cauca", 3.54, -76.30),
            city("Buenaventura", "Valle del Cauca", 3.88, -77.03),
            city("Tuluá", "Valle del Cauca", 4.08, -76.20),
            city("Buga", "Valle del Cauca", 3.90, -76.30),
            city("Cartago", "Valle del Cauca", 4.75, -75.91),
            remote("Mitú", "Vaupés", 1.25, -70.23),
            remote("Puerto Carreño", "Vichada", 6.19, -67.49)
    );

    private static final Map<String, City> BY_NAME = CITIES.stream()
            .collect(Collectors.toMap(c -> normalize(c.name()), c -> c));

    private ColombiaLocations() {
    }

    /** Busca una ciudad sin importar mayúsculas, espacios ni tildes. */
    public static Optional<City> find(String cityName) {
        return cityName == null ? Optional.empty() : Optional.ofNullable(BY_NAME.get(normalize(cityName)));
    }

    /** Departamentos en orden alfabético, cada uno con sus ciudades. */
    public static Map<String, List<String>> citiesByDepartment() {
        return CITIES.stream().collect(Collectors.groupingBy(
                City::department, LinkedHashMap::new, Collectors.mapping(City::name, Collectors.toList())));
    }

    // "Santa Marta", "santa marta " and "SANTA MARTA" are the same place; so are "Bogotá" and "Bogota"
    public static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }

    private static City city(String name, String department, double latitude, double longitude) {
        return new City(name, department, latitude, longitude, false);
    }

    private static City remote(String name, String department, double latitude, double longitude) {
        return new City(name, department, latitude, longitude, true);
    }
}
