# DevSync AI

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](#)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=111827)](#)
[![TypeScript](https://img.shields.io/badge/TypeScript-Vite-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-localhost%3A5433-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](#)
[![Auth](https://img.shields.io/badge/Auth-JWT%20%2B%20RBAC-111827?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](#)

DevSync AI is a full-stack enterprise project management and analytics platform for software teams. It brings together project workspaces, Kanban execution, sprint planning, bug tracking, threaded collaboration, activity visibility, and KPI analytics behind a secure JWT-authenticated API.

This repository is built as a production-minded full-stack application rather than a demo shell: the backend owns authentication, authorization, persistence, migrations, project-management workflows, and analytics; the frontend delivers a protected React workspace with routed project surfaces and typed API integration.

## What It Does

DevSync AI models the core workflow of an engineering organization:

- Users authenticate through stateless JWT-based login and registration.
- Organizations contain projects and project membership.
- Projects expose an authenticated workspace for overview, board, bugs, sprints, comments, activity, and analytics.
- Tasks move through a Kanban workflow with status, priority, assignee, due date, ordering, and sprint assignment.
- Bugs are tracked with severity, status, reporter, assignee, and optional task linkage.
- Comments attach to tasks and bugs to keep collaboration close to the work.
- Activity logs capture project events for an auditable timeline.
- Analytics APIs return KPI and chart-ready data for project health dashboards.

## Screenshots

Screenshots are intentionally separated by product area so recruiters and reviewers can scan the application quickly once images are added.

| Area | Preview |
|------|---------|
| Project Dashboard | `docs/screenshots/project-dashboard.png` |
| Kanban Task Board | `docs/screenshots/task-board.png` |
| Bug Tracker | `docs/screenshots/bug-tracker.png` |
| Sprint Planning | `docs/screenshots/sprint-planning.png` |
| Analytics Dashboard | `docs/screenshots/analytics-dashboard.png` |

## Architecture

DevSync AI is organized as a layered monolith for the MVP phase. The design keeps deployment simple while preserving clear boundaries between security, REST APIs, application services, persistence, analytics, and UI features.

```text
React 18 + Vite + TypeScript frontend
        |
        | /api/v1 over Axios
        v
Spring Security JWT filter
        |
        v
Spring Boot REST controllers
        |
        v
Application services
        |
        v
Spring Data JPA repositories
        |
        v
PostgreSQL database on localhost:5433
```

## Repository Structure

```text
DevSync-AI/
|-- backend/
|   |-- src/main/java/com/devsync/ai/
|   |   |-- analytics/          Project analytics service layer
|   |   |-- api/dto/            Auth, PM, and analytics DTOs
|   |   |-- config/             Security, JWT, CORS, seed configuration
|   |   |-- model/              JPA entities and enums
|   |   |-- pm/                 Project-management application services
|   |   |-- repository/         Spring Data JPA repositories
|   |   |-- rest/               REST controllers
|   |   `-- security/           JWT token service, filter, user details
|   `-- src/main/resources/
|       |-- application.yml
|       `-- db/migration/       Flyway migrations
|
|-- frontend/
|   |-- src/app/                Store, router, shell, layout
|   |-- src/features/           Auth, PM API client, feature state
|   |-- src/pages/              Route-level screens
|   `-- src/shared/             Shared UI, HTTP client, utilities
|
`-- docs/
    |-- architecture.md
    |-- project-management.md
    `-- database/
```

## Tech Stack

| Layer | Technologies |
|-------|--------------|
| Backend | Java 21, Spring Boot 3.2.5, Spring Web |
| Security | Spring Security, JWT, BCrypt, role-based access control |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Migrations | Flyway |
| Frontend | React 18, TypeScript, Vite |
| UI | Tailwind CSS |
| State & Routing | Redux Toolkit, React Redux, React Router |
| API Client | Axios |
| Testing | Spring Boot Test, Spring Security Test |
| Tooling | Maven, npm, ESLint |

## Backend Capabilities

The backend is a Spring Boot API under `backend/` with secured `/api/v1` routes. It includes:

- JWT authentication endpoints for registration and login.
- Protected profile and project-management APIs.
- Project membership and role checks through the PM authorization service.
- REST controllers for projects, members, tasks, bugs, sprints, comments, activity, and analytics.
- JPA entities for users, roles, organizations, memberships, projects, tasks, bugs, sprints, comments, and activity logs.
- Flyway migrations as the source of truth for database schema evolution.
- Optional demo seed support for project-management sample data.

Representative routes:

| Module | Routes |
|--------|--------|
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Profile | `GET /api/v1/me` |
| Organizations | `GET /api/v1/me/organizations` |
| Projects | `GET /api/v1/organizations/{organizationId}/projects`, `GET /api/v1/projects/{projectId}` |
| Tasks | `GET /api/v1/projects/{projectId}/tasks`, `GET /api/v1/projects/{projectId}/tasks/board` |
| Bugs | `GET /api/v1/projects/{projectId}/bugs`, `PATCH /api/v1/bugs/{bugId}` |
| Sprints | `GET /api/v1/projects/{projectId}/sprints`, `PATCH /api/v1/sprints/{sprintId}` |
| Comments | `GET /api/v1/tasks/{taskId}/comments`, `GET /api/v1/bugs/{bugId}/comments` |
| Activity | `GET /api/v1/projects/{projectId}/activity` |
| Analytics | `GET /api/v1/projects/{projectId}/analytics` |

## Frontend Capabilities

The frontend is a React 18 application under `frontend/` built with Vite, TypeScript, Tailwind CSS, Redux Toolkit, React Router, and Axios.

Key routes include:

| Route | Purpose |
|-------|---------|
| `/login` | Authenticated session entry |
| `/register` | User registration |
| `/projects` | Project catalog and creation flow |
| `/projects/:projectId/overview` | Project details and workspace overview |
| `/projects/:projectId/board` | Kanban task board |
| `/projects/:projectId/bugs` | Bug tracking surface |
| `/projects/:projectId/sprints` | Sprint planning |
| `/projects/:projectId/comments` | Work item comment threads |
| `/projects/:projectId/activity` | Project activity timeline |
| `/projects/:projectId/analytics` | KPI analytics dashboard |

The client stores the JWT-backed session, guards private routes, and sends authenticated API requests through the shared HTTP layer.

## Local Development

### Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21 |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 10+ |
| PostgreSQL | 15+ |

### Database

The default backend configuration expects PostgreSQL at `localhost:5433` with database `devsync_ai`.

```sql
CREATE DATABASE devsync_ai;
CREATE USER devsync WITH PASSWORD 'devsync123';
GRANT ALL PRIVILEGES ON DATABASE devsync_ai TO devsync;
```

Backend database configuration is read from environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/devsync_ai
SPRING_DATASOURCE_USERNAME=devsync
SPRING_DATASOURCE_PASSWORD=devsync123
SPRING_DATASOURCE_POOL_MAX=10
```

Flyway migrations run automatically on backend startup from:

```text
backend/src/main/resources/db/migration/
```

### Backend Setup

```bash
cd backend
cp env.example .env
mvn spring-boot:run
```

Useful backend URLs:

| URL | Purpose |
|-----|---------|
| `http://localhost:8080/api/v1/auth/register` | Register a user |
| `http://localhost:8080/api/v1/auth/login` | Log in and receive a JWT |
| `http://localhost:8080/api/v1/me` | Fetch authenticated profile |
| `http://localhost:8080/actuator/health` | Health check |

### Frontend Setup

```bash
cd frontend
cp env.example .env
npm install
npm run dev
```

The Vite app runs at:

```text
http://localhost:5173
```

During development, the frontend can proxy `/api` requests to the backend. The API base can also be configured with:

```env
VITE_API_BASE_URL=http://localhost:8080
```

## Validation Commands

```bash
cd backend
mvn test
```

```bash
cd frontend
npm run build
```

```bash
cd frontend
npm run lint
```

## Engineering Highlights

- Built a secure stateless authentication flow with Spring Security, JWT signing, BCrypt password hashing, and protected REST routes.
- Designed a project-management domain with organizations, memberships, projects, project members, tasks, bugs, sprints, comments, and activity logs.
- Implemented project-scoped authorization that accounts for application roles, organization roles, and project team roles.
- Used Flyway migrations and `ddl-auto: validate` so the database schema is explicit, versioned, and reviewable.
- Kept backend controllers thin by moving business behavior into focused application services.
- Added analytics APIs that return task, bug, sprint, workload, and KPI data for frontend dashboards.
- Built a typed React workspace with protected routes, reusable UI components, Redux-backed auth state, and Axios-based API integration.
- Organized the product into real recruiter-visible surfaces: project overview, Kanban board, bug tracker, sprint planning, comments, activity, and analytics.

## Future Roadmap

- Refresh-token rotation or httpOnly cookie sessions for stronger production security.
- Real-time project updates through WebSockets or server-sent events.
- Advanced task and bug filtering with saved views.
- File attachments for tasks, bugs, and comments.
- Velocity, cycle time, lead time, burndown, and forecasting analytics.
- AI-assisted sprint summaries, delivery-risk detection, and backlog recommendations.
- Dockerized local environment and production deployment profile.
- CI workflow for backend tests, frontend build, linting, and migration validation.

## Documentation

- [Architecture Overview](docs/architecture.md)
- [Project Management Module](docs/project-management.md)
- [Database Schema Overview](docs/database/schema-overview.md)
- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)

## Resume Summary

Built DevSync AI, a full-stack enterprise project management and analytics platform using Java 21, Spring Boot 3.2.5, Spring Security, JWT, JPA/Hibernate, Flyway, PostgreSQL, React 18, TypeScript, Vite, Tailwind CSS, Redux Toolkit, and React Router. Delivered secure authentication, RBAC-protected APIs, project workspaces, Kanban task management, bug tracking, sprint planning, comments, activity timelines, analytics dashboards, and PostgreSQL schema migrations.

## Author

**JeyAkash**

Developer of DevSync AI.

## License

This project is maintained as a professional portfolio and internship application project. Add a license before public reuse or distribution.
