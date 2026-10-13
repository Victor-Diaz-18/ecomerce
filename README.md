# University Store (ecomerce)

Sistema de gestión de tienda universitaria con catálogo de productos, clientes, inventario y órdenes de compra.

## Tecnologías

- **Backend:** Java 21+, Spring Boot 4, Maven, PostgreSQL, Testcontainers
- **Frontend:** React 18, TypeScript, Vite, React Router
- **Infraestructura:** Docker Compose (PostgreSQL 16)

## Requisitos

- Java 21+
- Node.js 20+
- Docker Desktop

## Inicio rápido

```bash
# 1. Clonar el repositorio
git clone https://github.com/Victor-Diaz-18/ecomerce.git
cd ecomerce

# 2. Instalar dependencias (solo la primera vez; también instala las del frontend)
npm install

# 3. Arrancar todo: base de datos + backend + frontend
npm run dev
```

Abre `http://localhost:3000`. Los logs del backend salen con el prefijo `[API]` y los del frontend con `[WEB]`.
Con `Ctrl + C` se detiene **todo**: backend, frontend y la base de datos de Docker (los datos se conservan).
Si cierras la terminal de golpe y Postgres queda encendido, apágalo con `npm run stop`.

| Servicio | URL |
|---|---|
| Frontend (Vite) | `http://localhost:3000` |
| API REST | `http://localhost:8080/api/v1` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

El frontend hace proxy de `/api` al backend, así que desde el navegador todo se usa por el puerto 3000.

### Otros comandos

| Comando | Qué hace |
|---|---|
| `npm run db` | Solo levanta PostgreSQL |
| `npm run stop` | Detiene PostgreSQL |
| `npm run backend` | Solo el backend |
| `npm run frontend` | Solo el frontend |
| `npm test` | Tests del backend |
| `npm run build` | Genera un único `.jar` con backend + frontend |

## Producción: un solo `.jar`

```bash
npm run build          # o ./mvnw package
java -jar target/university-store-0.0.1-SNAPSHOT.jar
```

Maven compila el frontend con Vite y lo mete dentro del `.jar`, así que todo (página web, API y Swagger)
queda en `http://localhost:8080`, sin necesidad de Vite ni de Node para ejecutarlo.
Para empaquetar solo el backend: `./mvnw package -Dskip.frontend=true`.

## Base de datos

### Migrar a otro PC

```bash
# En el PC origen (con datos reales):
docker exec university-postgres pg_dump -U postgres --clean --if-exists university_store > respaldo.sql
git add respaldo.sql
git commit -m "actualizar dump"
git push

# En el PC destino:
git pull
docker compose up -d
Get-Content respaldo.sql | docker exec -i university-postgres psql -U postgres university_store
```

### Variables de entorno (opcional)

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/university_store` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `1234` |

## Tests

```bash
npm test        # o ./mvnw test
```

Los tests usan Testcontainers y requieren Docker corriendo.

## Estructura del proyecto

```
ecomerce/
├── src/                          # Backend (Spring Boot)
│   ├── main/java/.../
│   │   ├── controller/           # REST controllers
│   │   ├── service/              # Lógica de negocio
│   │   ├── repository/           # Acceso a datos (Spring Data JPA)
│   │   ├── entity/               # Entidades JPA
│   │   ├── dto/                  # Objetos de transferencia
│   │   ├── mapper/               # MapStruct mappers
│   │   ├── exception/            # Manejo de errores
│   │   ├── config/               # Configuraciones
│   │   └── enums/                # Enumeraciones
│   └── test/                     # Tests
├── frontend/                     # Frontend (React + Vite)
│   └── src/
│       ├── pages/                # Páginas del sistema
│       ├── components/           # Componentes reutilizables
│       ├── services/             # API calls
│       ├── hooks/                # Custom hooks
│       ├── context/              # Contextos (Toast)
│       ├── types/                # Interfaces TypeScript
│       └── utils/                # Utilidades
├── scripts/mvnw.js               # Lanza Maven Wrapper en Windows/Linux/macOS
├── package.json                  # Scripts para arrancar todo junto
├── compose.yaml                  # Docker Compose
├── respaldo.sql                  # Dump de la base de datos
└── pom.xml                       # Configuración Maven
```
