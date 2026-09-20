# Inventory, Sales and Expense Management System

This repository contains a Java 21 Spring Boot backend and a Vite React frontend for an inventory and stock ledger. The project is Dockerized for local development and uses Flyway to own the PostgreSQL schema.

## Current status

The project has been corrected to align its Java version, schema contract, and local setup guidance with the codebase:

- Backend requires Java 21; Docker builds from Temurin 21 and Maven is configured for Java 21.
- The database schema migration aligns the entity primary keys and related foreign keys to BIGINT, matching the JPA model.
- The default admin bootstrap preserves an untouched legacy admin but also honors `.env` values for `ADMIN_DEFAULT_USERNAME` and `ADMIN_DEFAULT_PASSWORD`.
- The frontend is a working login/dashboard shell, and the public Swagger endpoint is documented as `/swagger-ui/index.html`.
- The app still has unimplemented inventory/suppliers/sales/expenses/reports routes, but those are explicitly marked as pending rather than pretending they exist.

## Stack

- Java 21
- Spring Boot 3.3.4
- PostgreSQL 16
- Flyway SQL migrations
- Spring Security + JWT
- React 18 + Vite + Tailwind
- Springdoc OpenAPI

## Local prerequisites

Use Java 21 before running Maven directly:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java 21.0.8-tem
java -version
```

If you do not use SDKMAN, install JDK 21 on your machine and set `JAVA_HOME` to the correct JDK before running Maven or the app.

## Quick start

1. Copy the env template:

   ```bash
   cp .env.example .env
   ```

2. Edit the values as needed:

   ```bash
   POSTGRES_DB=ismes
   POSTGRES_USER=ismes_user
   POSTGRES_PASSWORD=changeme
   JWT_SECRET=replace-with-a-strong-secret
   JWT_EXPIRATION_MS=86400000
   ADMIN_DEFAULT_USERNAME=admin
   ADMIN_DEFAULT_PASSWORD=admin123
   ```

3. Start the stack:

   ```bash
   docker compose up -d --build
   ```

   This starts:
   - `ismes-db` on port `5432`
   - `ismes-backend` on port `8080`
   - the Vite frontend on port `5173` when started separately

4. Check health:

   ```bash
   curl http://localhost:8080/actuator/health
   ```

5. Sign in with the default bootstrap user:

   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

6. Open the API docs:

   ```text
   http://localhost:8080/swagger-ui/index.html
   ```

## Project layout

```text
ismes/
├── docker-compose.yml
├── .env.example
├── README.md
├── PROJECT_INDEX.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/joseph/ismes/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── exception/
│       │   │   ├── repository/
│       │   │   ├── security/
│       │   │   └── service/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/migration/
│       └── test/java/com/joseph/ismes/
├── frontend/
│   ├── package.json
│   ├── src/
│   └── vite.config.js
└── scripts/
```

## Important implementation notes

- Flyway owns the database schema and Hibernate validates it with `ddl-auto: validate`.
- The migration file `backend/src/main/resources/db/migration/V3__align_identifier_types.sql` fixes the integer-to-bigint mismatch between entity IDs and the live PostgreSQL schema.
- The seeded admin is created or updated by `AdminBootstrap`, and the env values are read from `app.admin.default-username` and `app.admin.default-password` in `application.yml`.
- JWT requests that are malformed or expired are rejected by the filter before the request reaches protected endpoints.

## Frontend usage

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and sign in with the backend credentials. The dashboard and auth flow are working; the remaining sidebar sections are intentionally grayed out as pending features until their pages are implemented.

## Known pending areas

The following features are not fully implemented yet and should not be treated as complete:

- Inventory management pages
- Suppliers and purchase workflows
- Sales flow and authorization rules
- Expenses pages and reporting
- PDF export / receipts
- Notifications and activity logs

These gaps are documented in the project index and are separate from the startup issue fixed here.
