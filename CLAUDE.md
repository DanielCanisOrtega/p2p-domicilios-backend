# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

P2P Domicilios Backend - A Spring Boot REST API for peer-to-peer delivery services with geospatial driver matching using PostgreSQL/PostGIS.

**Stack**: Spring Boot 4.0.5, Java 17, PostgreSQL with PostGIS, Hibernate Spatial, Maven

## Build & Run Commands

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Package as JAR
./mvnw package
```

The application runs on port 8080 by default.

## Architecture

### Layered Structure
- **Controllers** (`controllers/`) - REST endpoints, request/response handling
- **Services** (`services/`) - Business logic layer
- **Repositories** (`repositories/`) - Data access with Spring Data JPA + custom PostGIS queries
- **Entities** (`entities/`) - JPA entities with geospatial support
- **DTOs** (`dto/`) - Data transfer objects
- **Config** (`config/`) - Spring configuration classes (Security, etc.)
- **Security** (`security/`) - JWT filters and security utilities
- **Exception** (`exception/`) - Global exception handlers
- **Enums** (`enums/`) - Application enums (Role, etc.)

### Key Technical Details

**Geospatial Architecture:**
- Uses JTS (JTS Topology Suite) with Hibernate Spatial for geometric types
- All geographic coordinates use SRID 4326 (WGS84 lat/lon)
- `Domiciliario` entity maintains both `latitud/longitud` fields (Double) AND a `location` field (PostGIS Point)
- `@PrePersist` and `@PreUpdate` hooks automatically sync lat/lon → PostGIS Point geometry
- Native PostGIS queries use `ST_DWithin` for radius filtering and `ST_Distance` for distance ordering
- PostGIS operations cast to `geography` type for accurate distance calculations in meters

**Entity Location Sync Pattern:**
```java
// In Domiciliario entity - location is automatically computed from latitud/longitud
private Double latitud;
private Double longitud;

@JsonIgnore
@Column(columnDefinition = "geometry(Point,4326)")
private Point location;  // Synced via @PrePersist/@PreUpdate
```

**PostGIS Query Pattern:**
```java
// Native query in DomiciliarioRepository
ST_DWithin(d.location::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radiusMeters)
```

### Domain Model

**User (Usuario):**
- Fields: `id`, `username` (unique), `password` (BCrypt), `email` (unique), `role` (CLIENT/DOMICILIARIO), `nombre`, `telefono`, `estado`, `fechaRegistro`, `enabled`
- Implements Spring Security's `UserDetails` interface
- Authorities derived from role field

**Domiciliario (Delivery Driver):**
- Fields: `disponible`, `verificado`, `latitud`, `longitud`, `location` (PostGIS Point)
- Nearby driver search filters by `disponible=true` AND `verificado=true`
- According to data model, should have FK to User entity (not yet implemented)

**Servicio (Delivery Order):**
- Fields: `estado`, `fecha_solicitud`, origin/destination addresses and coordinates
- Currently uses FK integers (`id_cliente`, `id_domiciliario`) - proper entity relationships not yet implemented

## Database

**PostgreSQL on Neon DB** with PostGIS extension enabled.

- Connection configured in `application.yaml`
- Hibernate DDL mode: `update` (auto-schema updates)
- PostGIS dialect: `org.hibernate.spatial.dialect.postgis.PostgisDialect`
- SQL logging enabled (`show-sql: true`, `format_sql: true`)

**Database requires PostGIS extension:**
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

## Security

**JWT Authentication:**
- Uses JWT (JSON Web Tokens) for stateless authentication
- BCrypt password encoding
- JWT secret and expiration configured in `application.yaml`
- Token format: `Authorization: Bearer {token}`
- JwtAuthenticationFilter intercepts requests and validates tokens
- UserDetailsService loads user from database

**Authorization:**
- Two roles: `CLIENT` and `DOMICILIARIO` (defined in `Role` enum)
- Authorities prefixed with `ROLE_` (e.g., `ROLE_CLIENT`, `ROLE_DOMICILIARIO`)

**Public Endpoints (no authentication required):**
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `GET /drivers/nearby` - Find nearby drivers

**Protected Endpoints (JWT required):**
- All other endpoints require valid JWT token in Authorization header

## API Endpoints

**Authentication:**
- `POST /auth/register` - Register new user (public)
  - Body: `{ "username", "password", "email", "role": "CLIENT"|"DOMICILIARIO", "nombre", "telefono" }`
  - Returns: `{ "token", "username", "email", "role", "userId" }`
- `POST /auth/login` - Login user (public)
  - Body: `{ "username", "password" }`
  - Returns: `{ "token", "username", "email", "role", "userId" }`

**Domiciliario (Drivers):**
- `GET /drivers/nearby?lat={lat}&lon={lon}&radiusKm={radius}` - Find nearby available drivers (public)

**Servicio (Orders):**
- `POST /api/orders/create` - Create new service request (requires JWT)

## Development Notes

- Uses Lombok: ensure annotation processing is enabled in your IDE
- GeometryFactory uses SRID 4326 (WGS84) consistently
- When adding geospatial entities, follow the Domiciliario pattern: maintain both lat/lon fields and PostGIS Point, sync via lifecycle hooks
- Repository queries involving distance should cast to `geography` type for accurate meter-based calculations
- Default search radius for nearby drivers: 3.0 km
