# Inventory, Sales and Expense Management System

This repository contains a Java 21 Spring Boot backend and a Vite React frontend for an inventory and stock ledger. The project is Dockerized for local development and uses Flyway to own the PostgreSQL schema.

## Current status

The project includes working inventory, procurement, sales, expense, reporting, and authentication workflows:

- Backend requires Java 21; Docker builds from Temurin 21 and Maven is configured for Java 21.
- The database schema migration aligns the entity primary keys and related foreign keys to BIGINT, matching the JPA model.
- The default admin bootstrap preserves an untouched legacy admin but also honors `.env` values for `ADMIN_DEFAULT_USERNAME` and `ADMIN_DEFAULT_PASSWORD`.
- The frontend provides active Inventory, Suppliers, Purchases, Sales, Expenses, and Reports pages.
- Fresh databases receive realistic demo data through Flyway migration V4 so customer walkthroughs are populated immediately.
- The dashboard calculates same-day sales, cost of goods, expenses, estimated profit, recent activity, and low-stock products.

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

### Demo walkthrough data

The first clean database includes demo records for:

- 4 products across phones, televisions, audio, and accessories
- 2 suppliers
- 2 purchases with purchase items
- 2 sales with sale items
- 2 expenses

The demo admin credentials are `admin` / `admin123`. Change them through `.env` before using the system outside local demonstrations. Migration V4 is applied once by Flyway and does not reinsert the demo records on later restarts.
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
- The migration file `backend/src/main/resources/db/migration/V4__seed_demo_data.sql` adds idempotent customer walkthrough data to a fresh database.
- The seeded admin is created or updated by `AdminBootstrap`, and the env values are read from `app.admin.default-username` and `app.admin.default-password` in `application.yml`.
- JWT requests that are malformed or expired are rejected by the filter before the request reaches protected endpoints.
- Purchases increase stock and update the latest buying price; sales decrease stock and reject insufficient inventory.

## Frontend usage

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and sign in with the backend credentials. The dashboard and operational pages are backed by the live API.

## Known pending areas

The following features remain future enhancements:

- PDF export / receipts
- Notifications and activity logs

These gaps are documented in the project index and are separate from the startup issue fixed here.
