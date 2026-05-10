-- Project lifecycle & team
CREATE TYPE project_mgmt_status AS ENUM ('ACTIVE', 'ARCHIVED', 'ON_HOLD');
CREATE TYPE proj_team_role AS ENUM ('PROJECT_OWNER', 'PROJECT_ADMIN', 'MEMBER', 'VIEWER');

ALTER TABLE projects
    ADD COLUMN description TEXT,
    ADD COLUMN status project_mgmt_status NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN owner_id UUID REFERENCES users (id) ON DELETE SET NULL;

CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role proj_team_role NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_user ON project_members (user_id);
CREATE INDEX idx_project_members_project ON project_members (project_id);

-- Sprints must exist before task FK
CREATE TYPE pm_sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'COMPLETED');

CREATE TABLE sprints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    goal TEXT,
    status pm_sprint_status NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sprints_project ON sprints (project_id);

-- Tasks & bugs (module tables; legacy work_items remain unused by this API)
CREATE TYPE pm_task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
CREATE TYPE pm_task_status AS ENUM ('BACKLOG', 'TODO', 'IN_PROGRESS', 'REVIEW', 'DONE');

CREATE TABLE pm_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    sprint_id UUID REFERENCES sprints (id) ON DELETE SET NULL,
    title VARCHAR(512) NOT NULL,
    description TEXT,
    priority pm_task_priority NOT NULL DEFAULT 'MEDIUM',
    status pm_task_status NOT NULL DEFAULT 'BACKLOG',
    assignee_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    due_date DATE,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pm_tasks_project ON pm_tasks (project_id);
CREATE INDEX idx_pm_tasks_sprint ON pm_tasks (sprint_id);
CREATE INDEX idx_pm_tasks_status ON pm_tasks (project_id, status);

CREATE TYPE pm_bug_severity AS ENUM ('MINOR', 'MAJOR', 'CRITICAL', 'BLOCKER');
CREATE TYPE pm_bug_status AS ENUM ('OPEN', 'TRIAGED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');

CREATE TABLE pm_bugs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    task_id UUID REFERENCES pm_tasks (id) ON DELETE SET NULL,
    title VARCHAR(512) NOT NULL,
    description TEXT,
    severity pm_bug_severity NOT NULL DEFAULT 'MINOR',
    status pm_bug_status NOT NULL DEFAULT 'OPEN',
    reporter_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    assignee_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pm_bugs_project ON pm_bugs (project_id);

-- Polymorphic comments for module tasks & bugs
CREATE TYPE pm_comment_subject AS ENUM ('TASK', 'BUG');

CREATE TABLE pm_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    subject_type pm_comment_subject NOT NULL,
    subject_id UUID NOT NULL,
    author_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    parent_comment_id UUID REFERENCES pm_comments (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pm_comments_subject ON pm_comments (subject_type, subject_id);

-- Activity log for project management changes
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL,
    project_id UUID REFERENCES projects (id) ON DELETE CASCADE,
    actor_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_logs_project_time ON activity_logs (project_id, created_at DESC);
