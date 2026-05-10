# Database schema overview

The canonical schema is applied by **Flyway** migrations in:

`backend/src/main/resources/db/migration/`

## Tenancy model

Most business tables include `organization_id` for multi-tenant isolation at the application layer. Enforce authorization in Spring Security **after** resolving the user’s memberships.

## Authentication

Password-based accounts use `users.password_hash` (BCrypt) and `roles` / `user_roles` (e.g. `USER`, `ADMIN`). JWTs are issued by the API and validated on each request (`Authorization: Bearer`).

## Core entities

| Area | Tables |
|------|--------|
| Identity | `organizations`, `users`, `memberships`, `roles`, `user_roles` |
| Teams | `teams`, `team_members` |
| Work | `projects` (see V3: `description`, `status`, `owner_id`), legacy `work_items` / `comments` / `activity_feed`, **PM module** `project_members`, `sprints`, `pm_tasks`, `pm_bugs`, `pm_comments`, `activity_logs` |
| DevOps | `integration_connections`, `repository_links`, `pipelines`, `deployment_events`, `environments` |
| Incidents | `incidents`, `on_call_rotations` |
| AI | `ai_conversations`, `ai_messages`, `ai_jobs`, `embedding_documents` |
| Platform | `api_keys`, `audit_events` |

## Extensions

- `pgcrypto` is enabled in `V1__init_schema.sql` for `gen_random_uuid()` compatibility across PostgreSQL versions.

## Project management (Flyway V3)

See migration `V3__project_management_module.sql` for enums (`project_mgmt_status`, `proj_team_role`, sprint/task/bug enums), `project_members`, `sprints`, `pm_tasks`, `pm_bugs`, `pm_comments` (TASK/BUG subjects), and `activity_logs`. A concise DDL copy lives at the end of `docs/database/schema.sql`.

Full API and access rules: [`docs/project-management.md`](../project-management.md).

Project analytics are computed from the PM tables above and do not require additional tables.

## Optional: pgvector

For semantic RAG, add the `vector` extension in a future migration and alter `embedding_documents` accordingly.
