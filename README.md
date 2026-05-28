# ecomerce

## Backend

### Requisitos
- Java 21

### Ejecutar en local (dev)
Define variables de entorno:

```bash
DB_URL=jdbc:postgresql://localhost:5433/university_store
DB_USERNAME=postgres
DB_PASSWORD=1234
./mvnw spring-boot:run
```

### Tests
Usa Testcontainers (Docker) para Postgres:

```bash
./mvnw test
```
