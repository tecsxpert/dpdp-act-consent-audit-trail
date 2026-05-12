-- ============================================================
-- V3 — Seed default admin user
-- Password is BCrypt of 'Admin@1234'
-- Change immediately after first login
-- ============================================================

INSERT INTO users (
    full_name,
    email,
    password,
    enabled
)
VALUES (
    'System Admin',
    'admin@internship.com',
    '$2a$10$jGdjVRO.7671n/VqTxpOpO.hXA73qvo3u0rM5PDP2wlqU2jFpQHL2',
    TRUE
);

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN'
FROM   users
WHERE  email = 'admin@internship.com';