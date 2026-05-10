<<<<<<< HEAD
# DevSync AI

Production-oriented scaffold for an AI-powered DevOps and team collaboration platform.

## Structure

| Path | Description |
|------|-------------|
| `backend/` | Spring Boot API (Java 21, PostgreSQL, JWT, Spring Security) |
| `frontend/` | React SPA (Vite, Tailwind, Redux Toolkit, Axios, React Router) |
| `docs/` | Architecture notes and SQL reference |

## Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 15+

## Quick start

### Database

Create a database and user, then configure `backend` (see `backend/env.example`).

### Backend

```bash
cd backend
cp env.example .env
# Set SPRING_DATASOURCE_* and DEVSYNC_JWT_SECRET in your environment (see backend/env.example)
# PowerShell example: $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn spring-boot:run
```

API base: `http://localhost:8080/api/v1`  
Auth: `POST /api/v1/auth/register`, `POST /api/v1/auth/login` — then send `Authorization: Bearer <token>` to protected routes.  
Actuator health: `http://localhost:8080/actuator/health`

See `backend/README.md` for details.

### Frontend

```bash
cd frontend
cp env.example .env
npm install
npm run dev
```

App: `http://localhost:5173`

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — high-level system overview
- [`docs/project-management.md`](docs/project-management.md) — PM schema, API, RBAC, seed, UI routes
- [`docs/database/schema-overview.md`](docs/database/schema-overview.md) — ER notes
- `backend/src/main/resources/db/migration/` — Flyway migrations (source of truth)

## Configuration

- Backend environment variables are documented in `backend/env.example`.
- Frontend public env vars (`VITE_*`) are documented in `frontend/env.example`.

## License

Proprietary — update as needed.
=======
# DevSync-AI
>>>>>>> 62b302c6546f7ee84cbf0f985f9ae87a93a09960
