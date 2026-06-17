# University Store (ecomerce)

Sistema de gestión de tienda universitaria con catálogo de productos, clientes, inventario y órdenes de compra.

## Tecnologías

- **Backend:** Java 21, Spring Boot 3, Maven, PostgreSQL, Testcontainers
- **Frontend:** React 18, TypeScript, Vite, React Router
- **Infraestructura:** Docker Compose (PostgreSQL 16)

## Requisitos

- Java 21+
- Node.js 18+
- Docker Desktop

## Inicio rápido

```bash
# 1. Clonar el repositorio
git clone https://github.com/Victor-Diaz-18/ecomerce.git
cd ecomerce

# 2. Levantar la base de datos
docker compose up -d

# 3. Construir y ejecutar el backend
./mvnw spring-boot:run
```

El backend arranca en `http://localhost:8080`.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

El frontend arranca en `http://localhost:3000` y hace proxy de `/api` al backend.

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
./mvnw test
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
├── compose.yaml                  # Docker Compose
├── respaldo.sql                  # Dump de la base de datos
└── pom.xml                       # Configuración Maven
```
