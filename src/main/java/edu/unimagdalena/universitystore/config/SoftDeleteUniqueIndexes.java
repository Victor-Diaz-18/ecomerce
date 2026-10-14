package edu.unimagdalena.universitystore.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * El correo de un cliente, el SKU de un producto y el nombre de una categoría son únicos solo entre
 * los registros que no están borrados: así, después de borrar un cliente, otra persona puede
 * registrarse con ese correo. Hibernate solo sabe crear restricciones únicas sobre toda la tabla,
 * así que al arrancar se quita la vieja (si existe) y se crea un índice único parcial.
 */
@Component
@Order(0)
@RequiredArgsConstructor
public class SoftDeleteUniqueIndexes implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SoftDeleteUniqueIndexes.class);

    private record UniqueColumn(String table, String column) {}

    private static final List<UniqueColumn> COLUMNS = List.of(
            new UniqueColumn("customers", "email"),
            new UniqueColumn("products", "sku"),
            new UniqueColumn("categories", "name"));

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        for (UniqueColumn uc : COLUMNS) {
            // Hibernate named the old constraint with a hash, so look it up by what it covers
            List<String> oldConstraints = jdbcTemplate.queryForList("""
                    SELECT c.conname FROM pg_constraint c
                    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
                    WHERE c.contype = 'u' AND c.conrelid = ?::regclass AND a.attname = ?
                      AND array_length(c.conkey, 1) = 1
                    """, String.class, uc.table(), uc.column());

            for (String name : oldConstraints) {
                jdbcTemplate.execute("ALTER TABLE " + uc.table() + " DROP CONSTRAINT " + name);
                log.info("Restricción única de {}.{} reemplazada por una que ignora los borrados", uc.table(), uc.column());
            }

            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + uc.table() + "_" + uc.column() + "_active_key"
                    + " ON " + uc.table() + " (" + uc.column() + ") WHERE deleted_at IS NULL");
        }
    }
}
