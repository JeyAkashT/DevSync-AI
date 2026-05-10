-- Reference copy of Flyway V1 baseline. Source of truth for migrations:
-- backend/src/main/resources/db/migration/V1__init_schema.sql

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- Organizations & identity
-- ---------------------------------------------------------------------------

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(128) NOT NULL UNIQUE,
    plan_tier VARCHAR(64) NOT NULL DEFAULT 'FREE',
    settings JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    external_auth_sub VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TYPE membership_role AS ENUM ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER');

CREATE TABLE memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role membership_role NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_memberships_user ON memberships (user_id);

-- ---------------------------------------------------------------------------
-- Teams
-- ---------------------------------------------------------------------------

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, slug)
);

CREATE TABLE team_members (
    team_id UUID NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (team_id, user_id)
);

-- ---------------------------------------------------------------------------
-- Projects & work
-- ---------------------------------------------------------------------------

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    key VARCHAR(32) NOT NULL,
    repository_url VARCHAR(1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, key)
);

CREATE TYPE work_item_type AS ENUM ('TASK', 'BUG', 'STORY');
CREATE TYPE work_item_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

CREATE TABLE work_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    type work_item_type NOT NULL DEFAULT 'TASK',
    status VARCHAR(64) NOT NULL DEFAULT 'BACKLOG',
    title VARCHAR(512) NOT NULL,
    description TEXT,
    assignee_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    priority work_item_priority NOT NULL DEFAULT 'MEDIUM',
    labels TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_work_items_project_status ON work_items (project_id, status);

CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_item_id UUID NOT NULL REFERENCES work_items (id) ON DELETE CASCADE,
    author_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    parent_comment_id UUID REFERENCES comments (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comments_work_item ON comments (work_item_id);

CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    storage_key VARCHAR(1024) NOT NULL,
    mime_type VARCHAR(255),
    byte_size BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attachments_org ON attachments (organization_id);

CREATE TABLE activity_feed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    actor_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    verb VARCHAR(64) NOT NULL,
    object_type VARCHAR(64) NOT NULL,
    object_id UUID NOT NULL,
    payload JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_feed_org_time ON activity_feed (organization_id, occurred_at DESC);

-- ---------------------------------------------------------------------------
-- Integrations & CI/CD
-- ---------------------------------------------------------------------------

CREATE TYPE integration_provider AS ENUM ('GITHUB', 'GITLAB', 'AZURE_DEVOPS', 'JENKINS', 'CUSTOM');

CREATE TABLE integration_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    provider integration_provider NOT NULL,
    display_name VARCHAR(255),
    encrypted_credentials TEXT,
    status VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    webhook_secret_hash VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (organization_id, provider)
);

CREATE TABLE repository_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    integration_id UUID NOT NULL REFERENCES integration_connections (id) ON DELETE CASCADE,
    external_repository_id VARCHAR(512) NOT NULL,
    default_branch VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (integration_id, external_repository_id)
);

CREATE TABLE pipelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    external_id VARCHAR(512) NOT NULL,
    name VARCHAR(512),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, external_id)
);

CREATE TYPE deployment_status AS ENUM ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'UNKNOWN');

CREATE TABLE deployment_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    pipeline_id UUID REFERENCES pipelines (id) ON DELETE SET NULL,
    external_run_id VARCHAR(512),
    status deployment_status NOT NULL DEFAULT 'UNKNOWN',
    ref_name VARCHAR(512),
    commit_sha VARCHAR(128),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deployments_project_started ON deployment_events (project_id, started_at DESC NULLS LAST);

CREATE TABLE environments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    protection_rules JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, name)
);

-- ---------------------------------------------------------------------------
-- Incidents & on-call
-- ---------------------------------------------------------------------------

CREATE TYPE incident_severity AS ENUM ('SEV1', 'SEV2', 'SEV3', 'SEV4', 'INFO');
CREATE TYPE incident_status AS ENUM ('OPEN', 'MITIGATED', 'RESOLVED', 'CANCELLED');

CREATE TABLE incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    title VARCHAR(512) NOT NULL,
    severity incident_severity NOT NULL DEFAULT 'INFO',
    status incident_status NOT NULL DEFAULT 'OPEN',
    opened_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_incidents_org ON incidents (organization_id);

CREATE TABLE on_call_rotations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    escalation_policy JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------
-- AI transcripts & jobs
-- ---------------------------------------------------------------------------

CREATE TABLE ai_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    created_by_user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    project_id UUID REFERENCES projects (id) ON DELETE SET NULL,
    title VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE ai_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES ai_conversations (id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    token_usage JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_messages_conv ON ai_messages (conversation_id);

CREATE TYPE ai_job_status AS ENUM ('QUEUED', 'RUNNING', 'COMPLETED', 'FAILED');

CREATE TABLE ai_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    status ai_job_status NOT NULL DEFAULT 'QUEUED',
    task_type VARCHAR(128) NOT NULL,
    input_ref VARCHAR(1024),
    output_ref TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_jobs_org ON ai_jobs (organization_id);

CREATE TABLE embedding_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    source_type VARCHAR(64) NOT NULL,
    source_id UUID NOT NULL,
    chunk_text TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_embedding_org_source ON embedding_documents (organization_id, source_type, source_id);

-- ---------------------------------------------------------------------------
-- API keys & audit
-- ---------------------------------------------------------------------------

CREATE TABLE api_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    key_hash VARCHAR(255) NOT NULL UNIQUE,
    scopes TEXT[] NOT NULL DEFAULT '{}',
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_org ON api_keys (organization_id);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES organizations (id) ON DELETE SET NULL,
    user_id UUID REFERENCES users (id) ON DELETE SET NULL,
    action VARCHAR(128) NOT NULL,
    resource_type VARCHAR(128) NOT NULL,
    resource_id UUID,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_org_time ON audit_events (organization_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- V2 — application roles & password auth (see Flyway V2 migration for authority)
-- ---------------------------------------------------------------------------

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(64) NOT NULL UNIQUE
);

INSERT INTO roles (name)
SELECT v
FROM (VALUES ('USER'), ('ADMIN')) AS t (v)
WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = t.v);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_role ON user_roles (role_id);

ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(255);

ALTER TABLE users
    ALTER COLUMN external_auth_sub DROP NOT NULL;

-- ---------------------------------------------------------------------------
-- V3 — project management module (canonical: V3__project_management_module.sql)
-- ---------------------------------------------------------------------------

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
