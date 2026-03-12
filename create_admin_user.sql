-- Script to create admin user
-- Run this after the application has started and initialized roles
-- Default password: 'admin123' (hashed with BCrypt)

-- First, find the ADMIN role ID
-- Then create an admin user

-- Insert admin user (password is 'admin123' hashed with BCrypt)
-- NOTE: You should change this password immediately after first login!
INSERT INTO users (name, email, password, real_name, surname, beginner, team, city, discord_nick)
VALUES ('Admin', 'admin@warbracket.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCy', 'System', 'Administrator', false, null, null, null)
ON CONFLICT (email) DO NOTHING;

-- Link admin user to ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, user_role_dict r
WHERE u.email = 'admin@warbracket.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Display confirmation
SELECT 'Admin user created successfully!' as status,
       'Email: admin@warbracket.com' as login_email,
       'Password: admin123' as default_password,
       'IMPORTANT: Change this password immediately!' as warning;
