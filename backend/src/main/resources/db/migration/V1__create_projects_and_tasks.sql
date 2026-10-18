CREATE TABLE projects (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE TABLE tasks (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id  BIGINT        NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    title       VARCHAR(200)  NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(20)   NOT NULL DEFAULT 'TODO'
                CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority    VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM'
                CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    due_date    DATE,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_tasks_project_status ON tasks (project_id, status);
