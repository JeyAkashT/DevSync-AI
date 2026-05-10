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
