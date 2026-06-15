# Solution — Inditex Supplier Management

## How to Start

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Country mock | http://localhost:8088 |

The first build takes a few minutes (Maven downloads dependencies, npm installs packages). Subsequent builds are faster thanks to Docker layer caching.

### Loading test data

Once the stack is up, seed the database with representative data for all workflow scenarios:

```bash
docker compose exec -T db psql -U supplier -d supplierdb < seed-data.sql
```

See [seed-data.sql](seed-data.sql) for a breakdown of what each row exercises.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 3.5.14 / Java 21 |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| HTTP client | Spring `RestClient` (introduced in 3.2) |
| Configuration | `@ConfigurationProperties` record |
| Testing | JUnit 5, Testcontainers, WireMock 3.6.0 |
| Frontend framework | React 18 + TypeScript 5 |
| Build tool | Vite 5 |
| Styling | Tailwind CSS 3 |
| Data fetching | TanStack Query v5 |
| Frontend tests | Vitest + Testing Library |
| Runtime images | `eclipse-temurin:21-jre-alpine`, `nginx:stable-alpine` |

---

## Architecture Decisions

### Hexagonal (Ports & Adapters) Architecture

The domain layer (`domain/model/`, `domain/exception/`, `domain/port/`) has zero Spring dependencies. This means:
- Domain logic is tested with pure JUnit 5 — no Spring context startup
- The ports (interfaces) are the contract; infrastructure adapts to them
- Swapping PostgreSQL for another database, or the country HTTP client for a queue consumer, requires changing only the infrastructure layer

The dependency flow is strict: **domain → application → infrastructure (persistence/rest)**. Inbound: HTTP request → controller → service → domain aggregate → repository port → JPA adapter.

### Application Layer: Service Classes

The application layer exposes two services:

- **`CandidateService`** — create, get, accept, refuse candidacies
- **`SupplierService`** — get supplier, ban supplier, get potential suppliers

Each method is a single transaction boundary.

### Database Design

Two tables: `candidates` (candidacy lifecycle) and `suppliers` (verified suppliers):

- DUNS is the primary key on both tables — the "one supplier per DUNS" invariant is enforced at the DB level as defense-in-depth
- `CHECK` constraints on `status` and `rating` columns mirror domain invariants
- Two partial indexes `WHERE status != 'DISQUALIFIED'` cover the most common query pattern (non-disqualified lookup and score ranking) and stay small at scale since disqualified rows are excluded

### Potential Suppliers — Single SQL Query with Window Functions

The score and bonus calculation runs entirely in PostgreSQL via a single CTE. Key choices:

1. **`DENSE_RANK()`** (not `RANK()`) handles tied turnovers. `RANK` assigns ranks 1, 1, 3 for two equal values — leaving the third at rank 3 and incorrectly skipping it out of the bonus pool. `DENSE_RANK` assigns 1, 1, 2 so the second unique value is at rank 2.

2. **The bonus pool uses all non-disqualified suppliers** in a country. The spec defines the two lowest unique turnovers per country without rate-scoping — a supplier counts toward the country's pool even if they don't appear in the current result set.

3. **`COUNT(*) OVER ()`** computes the total matching rows in the same pass as the SELECT, avoiding a second round-trip for pagination metadata.

4. **`LIMIT`/`OFFSET` pagination** — acceptable here because page size is capped at 10 and the query is already rate-filtered. Cursor-based pagination would be preferred for high-cardinality unbounded queries.

### Status Mapping: Internal vs API

Internally: `ACTIVE`, `ON_PROBATION`, `DISQUALIFIED`. The API contract exposes only `Active` and `Disqualified`. The mapping lives exclusively in `SupplierApiMapper.toApiStatus()`. Neither the domain nor the persistence layer knows about the API's simplified view.

### Country Service Client

`CountryServiceClient` implements the `CountryService` port using Spring's `RestClient`. It throws `CountryNotFoundException` on a 404 from the external service, which `GlobalExceptionHandler` maps to HTTP 422 (Unprocessable Content).

Rationale: a 404 from the country service means the country code is not recognized — this is a semantic error in the caller's request, not a missing resource in our own system.

The client's base URL is bound via a typed `@ConfigurationProperties` record (`CountryServiceProperties`), replacing a raw `@Value` injection. This provides IDE autocompletion, null-safety analysis, and compile-time binding validation.

### OpenAPI Conformance

All endpoints are validated against `wiki/itx-iop_tech-supplier_flow-main-openapi3_1.yaml`. One gap that was identified and fixed: `POST /candidates` listed HTTP 422 in the spec but the implementation never triggered it. Fixed by calling `countryService.isBanned(country)` at the start of `createCandidate` — if the country is unknown, `CountryNotFoundException` propagates and the global handler returns 422.

### Frontend State Architecture

- **Server state**: TanStack Query. Loading, error, stale, and cache states are handled automatically. `staleTime: 30s` prevents redundant refetches while the user filters or sorts.
- **UI state**: `useState` in `PotentialSuppliersPage`. `submittedRate` is set only on form submit (not on every keystroke) to avoid fetching on every key press.
- **Derived state**: filtering and sorting are pure `useMemo` transformations over the fetched page — no extra state for the filtered result set.
- **Pagination**: server-side via `limit`/`offset`. Filtering is client-side over the current page — the API has no filter parameters beyond `rate`.
- **nginx proxy**: the frontend image proxies `/suppliers` and `/candidates` to the backend container, eliminating CORS with no backend configuration.

---

## Docker

Both images use multi-stage builds. The build stage (Maven / Node) is discarded; only the runtime artifact is in the final image.

```
Backend:  eclipse-temurin:21-jre-alpine  + apk upgrade --no-cache
Frontend: nginx:stable-alpine            + apk upgrade --no-cache
```

`apk upgrade --no-cache` patches all Alpine packages on every build, keeping the images clean of known CVEs regardless of when the base image was last updated. The build stage also runs `apk upgrade` so Maven and its transitive downloads are not the source of leaked CVEs.

---

## Running Tests

### Backend

```bash
cd backend

# Unit tests only (no Docker required)
./mvnw test

# All tests including integration (requires Docker for Testcontainers)
./mvnw verify
```

Integration tests spin up a real PostgreSQL container and a WireMock server. WireMock uses `wiremock-standalone` to avoid classpath conflicts with Jetty.

### Frontend

```bash
cd frontend
npm install
npm test
```

Three test suites:
- `filterAndSort.test.ts` — pure unit tests for the filter/sort utilities
- `SearchBar.test.tsx` — user interaction: validation errors, submit fires callback
- `SuppliersTable.test.tsx` — rendering: loading state, error state, empty state, data rows, sort header clicks

---

## Trade-offs and Gaps

| Area | Decision | Reason |
|---|---|---|
| No authentication | Skipped | Out of scope for this test |
| Optimistic locking | Not implemented | Concurrent accepts on the same DUNS are caught by the PK constraint → `DataIntegrityViolationException` → 409 |
| Country service resilience | No circuit breaker | Resilience4j would be added in production; complexity not warranted here |
| Client-side filtering | Applied to current page only | The API has no filter params beyond `rate`; this is the natural boundary |
| Cursor pagination | Using `LIMIT`/`OFFSET` | Matches the OpenAPI spec exactly; cursor pagination would require API changes |

---

## Notable Considerations

- **WireMock stub convention**: countries starting A–M are not banned, N–Z are banned. Integration tests and the acceptance flow rely on this.
- **Integration test** (`SupplierIntegrationTest`): spins up real PostgreSQL via Testcontainers and a real WireMock server. Exercises the SQL window function query, Flyway migration, and the HTTP client together in one test.
- **Seed data** (`seed-data.sql`): 12 suppliers across ES/FR/PT/IT (all rating and status combinations), 3 candidates (PENDING, REFUSED, ACCEPTED-then-banned), designed to exercise pagination, the bonus calculation across countries with 1/2/3+ entries, and all error paths.
- **`@ConfigurationProperties` processor**: `spring-boot-configuration-processor` is declared as an optional dependency so the IDE resolves `country-service.base-url` in `application.yml` with autocompletion and no unknown-property warnings.
