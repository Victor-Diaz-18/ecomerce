package edu.unimagdalena.universitystore.config;

import edu.unimagdalena.universitystore.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hibernate crea un CHECK con los valores del enum al crear la tabla, pero con ddl-auto=update
 * no lo actualiza cuando se agrega un estado nuevo, y la base de datos rechazaría ese valor.
 * Al arrancar se vuelven a crear esas restricciones a partir de los valores actuales de OrderStatus.
 */
@Component
@RequiredArgsConstructor
public class OrderStatusConstraintUpdater implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(OrderStatusConstraintUpdater.class);

    private static final List<String> TABLES = List.of("orders", "order_status_history");

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String allowed = Arrays.stream(OrderStatus.values())
                .map(status -> "'" + status.name() + "'")
                .collect(Collectors.joining(", "));

        for (String table : TABLES) {
            String constraint = table + "_status_check";
            jdbcTemplate.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraint);
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD CONSTRAINT " + constraint
                    + " CHECK (status IN (" + allowed + "))");
        }
        log.info("Restricciones de estado de órdenes actualizadas: {}", allowed);
    }
}
