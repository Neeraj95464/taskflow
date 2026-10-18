-- A small demo board so the UI is not empty on first start.
INSERT INTO projects (name, description, created_at, updated_at)
VALUES ('Demo', 'Sample project created by the initial migration', now(), now());

INSERT INTO tasks (project_id, title, description, status, priority, created_at, updated_at)
SELECT p.id, t.title, t.description, t.status, t.priority, now(), now()
FROM projects p
CROSS JOIN (VALUES
    ('Set up CI pipeline', 'Build, test and package on every push', 'DONE', 'HIGH'),
    ('Write integration tests', 'Testcontainers + PostgreSQL', 'IN_PROGRESS', 'HIGH'),
    ('Add Helm chart', 'Deploy the stack to Kubernetes', 'TODO', 'MEDIUM')
) AS t (title, description, status, priority)
WHERE p.name = 'Demo';
