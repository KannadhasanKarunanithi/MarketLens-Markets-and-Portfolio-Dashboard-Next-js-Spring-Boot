# MarketLens

A markets and portfolio dashboard. Track instruments on watchlists, view
interactive price charts, record your holdings and trades, and see portfolio
performance, allocation and profit and loss with proper return math. It is a
tracker and analytics tool, not a broker, so there is no real order execution.

This is a personal project built to practice full stack development with Next.js
and Spring Boot. It is built in stages, one module at a time.

## Status

Early. The project scaffold is in place: a Spring Boot API with Flyway and a
Testcontainers backed test, and a Next.js frontend that reads the API.

| Module | State |
| --- | --- |
| Project setup and local environment | done |
| Authentication | not started |
| Instrument master and search | not started |
| Market data simulator and price history | not started |
| Instrument detail and charts | not started |
| Live quotes | not started |
| Watchlists | not started |
| Transactions | not started |
| Portfolio holdings and profit and loss | not started |
| Allocation and performance | not started |
| Price alerts | not started |
| Real market data provider | not started |
| Audit trail and deployment config | not started |

## Tech stack

Backend

- Java 21
- Spring Boot 3.5 (Web, Data JPA, Validation, Actuator)
- PostgreSQL 16
- Flyway for database migrations
- Maven, through the Maven wrapper
- JUnit 5 and Testcontainers for tests

Frontend

- Next.js with the App Router and TypeScript
- Tailwind CSS
- Charts are added in a later milestone

## Repository layout

```
backend/    Spring Boot API
frontend/   Next.js app
docs/       Design notes
```

## Prerequisites

- JDK 21 or newer
- Node.js 20 or newer
- Docker and Docker Compose, for the local PostgreSQL instance

## Running locally

### 1. Start PostgreSQL

```
docker compose up -d
```

PostgreSQL is published on host port 5435. Adminer is on http://localhost:8082
(server `db`, user `marketlens`, password `marketlens`, database `marketlens`).

### 2. Start the backend

```
cd backend
./mvnw spring-boot:run
```

On Windows Command Prompt use `mvnw spring-boot:run`, on PowerShell `.\mvnw spring-boot:run`.

The API runs on http://localhost:8090, on a non default port so it does not clash
with other local services. Health is at http://localhost:8090/actuator/health.
Set `SERVER_PORT` to change it.

### 3. Start the frontend

```
cd frontend
npm install
npm run dev
```

The app runs on http://localhost:3000 and proxies `/api` and `/actuator` to the
backend.

## Tests

Backend:

```
cd backend
./mvnw test
```

The tests start a throwaway PostgreSQL container, so Docker must be running.

Frontend:

```
cd frontend
npm run lint
npm run build
```

## License

MIT. See [LICENSE](LICENSE).
