# DevSync AI — Architecture Overview

This repository implements a layered monolith for the MVP phase:

- **Backend** (`backend/`): Spring Boot 3 on Java 21, REST API, **stateless JWT** (issued by the API, HS256), BCrypt passwords, JPA, Flyway.
- **Frontend** (`frontend/`): React 18, Vite, Redux Toolkit, React Router, Tailwind CSS.
- **Database**: PostgreSQL; schema owned by Flyway migrations under `backend/src/main/resources/db/migration/`.

For system context, deployment topology, and API conventions, see the root `README.md` and `docs/database/schema-overview.md`.

## Implemented modules

- Authentication and user profile APIs.
- Project management APIs for projects, members, tasks, bugs, sprints, comments, and activity.
- Project analytics KPIs at `/api/v1/projects/{projectId}/analytics`, surfaced in the React project workspace.

## Next steps

- Wire an OIDC provider (issuer URI) matching your JWT issuer.
- Add domain services, integration webhooks, and AI orchestration bounded context.
- Harden CORS origins for production and add rate limiting at the edge.
