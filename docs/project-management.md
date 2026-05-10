# Project management module

This document describes the DevSync AI **project management** surface: schema (Flyway V3), REST API, analytics KPIs, role checks, demo seed, and the React routes that exercise it.

## Schema

Applied by `backend/src/main/resources/db/migration/V3__project_management_module.sql`:

- **`projects`**: adds `description`, `status` (`project_mgmt_status`), `owner_id`.
- **`project_members`**: user ↔ project with `proj_team_role` (`PROJECT_OWNER`, `PROJECT_ADMIN`, `MEMBER`, `VIEWER`).
- **`sprints`**: name, dates, goal, `pm_sprint_status`.
- **`pm_tasks`**: title, description, priority, status, assignee, due date, optional `sprint_id`, ordering `position`.
- **`pm_bugs`**: linked to project and optional `pm_tasks`, severity, status, reporter, assignee.
- **`pm_comments`**: polymorphic `subject_type` TASK or BUG plus `subject_id`.
- **`activity_logs`**: audit rows for PM actions (JSON `payload`, indexed by project and time).

A reference DDL copy is appended to `docs/database/schema.sql`.

## REST API (`/api/v1`)

All routes require a valid JWT unless your security config exempts them. List endpoints typically support `page` and `size` (capped server-side).

| Area | Method | Path |
|------|--------|------|
| Directory | GET | `/me/organizations` |
| Org projects | GET, POST | `/organizations/{organizationId}/projects` |
| Org project (scoped) | GET | `/organizations/{organizationId}/projects/{projectId}` |
| Project | GET, PATCH, DELETE | `/projects/{projectId}` |
| Members | GET, POST, DELETE | `/projects/{projectId}/members` |
| Tasks | GET (list + filters), POST | `/projects/{projectId}/tasks` |
| Board | GET | `/projects/{projectId}/tasks/board` |
| Task | PATCH, DELETE | `/tasks/{taskId}` |
| Bugs | GET, POST | `/projects/{projectId}/bugs` |
| Bug | PATCH | `/bugs/{bugId}` |
| Sprints | GET, POST | `/projects/{projectId}/sprints` |
| Sprint | PATCH | `/sprints/{sprintId}` |
| Comments | GET, POST | `/tasks/{taskId}/comments`, `/bugs/{bugId}/comments` |
| Activity | GET | `/projects/{projectId}/activity` |
| Analytics | GET | `/projects/{projectId}/analytics` |

Query parameters for filtering (where implemented) are defined on the controllers (e.g. task status, full-text style search on catalog routes).

### Analytics response

`GET /projects/{projectId}/analytics` is project-scoped and requires at least project `VIEWER` access. It returns:

- `kpis`: totals and rates for tasks, bugs, overdue work, and sprints.
- `taskStatus`, `bugStatus`, `bugSeverity`, `sprintStatus`: complete enum-based count series, including zero-count buckets.
- `workload`: assigned and open task counts grouped by assignee.

## Role-based access

`PmAuthorizationService` enforces:

1. The caller must be an **organization member** for the project’s organization.
2. **Application `ROLE_ADMIN`** is treated as full **project owner** on all projects.
3. Organization **OWNER** or **ADMIN** is also treated as **project owner** (elevated access without a `project_members` row).
4. Otherwise the user must appear in **`project_members`**; operations may require a minimum `ProjTeamRole` (`VIEWER` < `MEMBER` < `PROJECT_ADMIN` < `PROJECT_OWNER` using enum order in code).

Individual service methods call `requireAtLeast` for mutating operations.

## Demo seed

When `DEVSYNC_SEED_ENABLED=true` (see `application.yml` under `devsync.seed`), the application runs a transactional seed that creates a demo user, organization, project, varied task/bug/sprint states for analytics, comments, and sample activity. Environment variables:

| Variable | Purpose |
|----------|---------|
| `DEVSYNC_SEED_ENABLED` | Master switch |
| `DEVSYNC_SEED_DEMO_EMAIL` | Demo account email |
| `DEVSYNC_SEED_DEMO_PASSWORD` | Demo account password |
| `DEVSYNC_SEED_ORG_SLUG` | Organization slug |

Documented in `backend/env.example`.

## Frontend

Authenticated routes (see `frontend/src/app/router.tsx`):

- `/projects` — list, search, create (org picker uses `devsync.selectedOrganizationId` in localStorage).
- `/projects/:projectId` — redirects to **`board`**.
- `/projects/:projectId/overview` — project detail and description edit.
- `/projects/:projectId/board` — Kanban columns by task status.
- `/projects/:projectId/bugs` — bug table and quick create.
- `/projects/:projectId/sprints` — sprint list and create form.
- `/projects/:projectId/comments` — task comment thread (pick a task from the board-loaded list).
- `/projects/:projectId/activity` — timeline from `activity_logs`.

- `/projects/:projectId/analytics` - KPI dashboard with task, bug, sprint, and workload charts.

The shell header includes a **Projects** link to `/projects`.
