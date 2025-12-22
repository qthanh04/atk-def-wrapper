-- =====================================================
-- SQL Script tạo Database cho ATK-DEF Backend
-- PostgreSQL Version
-- =====================================================

-- Kết nối vào database mydb trước khi chạy script này
-- psql -h localhost -U admin -d mydb

-- =====================================================
-- 1. TẠO CÁC BẢNG CƠ BẢN (KHÔNG CÓ FK)
-- =====================================================

-- Bảng roles: Lưu các vai trò người dùng
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Bảng tournaments: Lưu thông tin giải đấu
CREATE TABLE IF NOT EXISTS tournaments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP,
    end_date TIMESTAMP
);

-- Bảng labs: Lưu thông tin phòng lab
CREATE TABLE IF NOT EXISTS lab_entity (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    config JSONB,
    created_at TIMESTAMP
);

-- Bảng teams: Lưu thông tin đội thi đấu
CREATE TABLE IF NOT EXISTS teams (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    affiliation VARCHAR(255),
    country VARCHAR(100),
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. TẠO CÁC BẢNG CÓ FK LIÊN KẾT
-- =====================================================

-- Bảng users: Lưu thông tin người dùng
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(120),
    team_id INTEGER REFERENCES teams(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng user_roles: Bảng trung gian liên kết users và roles (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Bảng rounds: Lưu thông tin các hiệp đấu
CREATE TABLE IF NOT EXISTS rounds (
    id SERIAL PRIMARY KEY,
    tournament_id INTEGER NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    number INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL
);

-- Bảng challenges: Lưu thông tin các bài challenge
CREATE TABLE IF NOT EXISTS challenges (
    id SERIAL PRIMARY KEY,
    lab_id INTEGER NOT NULL REFERENCES lab_entity(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    service_port INTEGER,
    image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng service_entity: Lưu thông tin docker service của mỗi team
CREATE TABLE IF NOT EXISTS service_entity (
    id SERIAL PRIMARY KEY,
    challenge_id INTEGER NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
    team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    container_id VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng flags: Lưu các cờ (flags)
CREATE TABLE IF NOT EXISTS flags (
    id SERIAL PRIMARY KEY,
    service_id INTEGER NOT NULL REFERENCES service_entity(id) ON DELETE CASCADE,
    value VARCHAR(255) NOT NULL,
    round INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng submissions: Lưu các lần nộp cờ
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    flag_id INTEGER REFERENCES flags(id) ON DELETE SET NULL,
    submitted_value VARCHAR(255),
    is_valid BOOLEAN,
    round INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng scores: Lưu điểm số
CREATE TABLE IF NOT EXISTS scores (
    id SERIAL PRIMARY KEY,
    team_id INTEGER NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    round_id INTEGER NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    offense_score DOUBLE PRECISION,
    defense_score DOUBLE PRECISION,
    sla_score DOUBLE PRECISION,
    total_score DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng healthchecks: Lưu kết quả healthcheck
CREATE TABLE IF NOT EXISTS healthchecks (
    id SERIAL PRIMARY KEY,
    service_id INTEGER NOT NULL REFERENCES service_entity(id) ON DELETE CASCADE,
    round_id INTEGER NOT NULL REFERENCES rounds(id) ON DELETE CASCADE,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. INSERT DỮ LIỆU MẪU
-- =====================================================

-- Thêm các roles mặc định
INSERT INTO roles (name) VALUES 
    ('ROLE_TEACHER'),
    ('ROLE_STUDENT'),
    ('ROLE_TEAM')
ON CONFLICT (name) DO NOTHING;

-- Tạo user admin (Teacher) - Password: admin123 (BCrypt encoded)
-- BCrypt hash của "admin123" = $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3mPRe.VdM8iA61EWqKdS
INSERT INTO users (username, email, password) VALUES 
    ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3mPRe.VdM8iA61EWqKdS')
ON CONFLICT (username) DO NOTHING;

-- Gán role TEACHER cho user admin
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_TEACHER'
ON CONFLICT DO NOTHING;

-- =====================================================
-- 4. TẠO INDEX ĐỂ TỐI ƯU TRUY VẤN
-- =====================================================

-- Index cho bảng users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_team_id ON users(team_id);

-- Index cho bảng teams
CREATE INDEX IF NOT EXISTS idx_teams_username ON teams(username);
CREATE INDEX IF NOT EXISTS idx_teams_name ON teams(name);

-- Index cho bảng flags
CREATE INDEX IF NOT EXISTS idx_flags_service_id ON flags(service_id);
CREATE INDEX IF NOT EXISTS idx_flags_round ON flags(round);

-- Index cho bảng submissions
CREATE INDEX IF NOT EXISTS idx_submissions_team_id ON submissions(team_id);
CREATE INDEX IF NOT EXISTS idx_submissions_round ON submissions(round);

-- Index cho bảng scores
CREATE INDEX IF NOT EXISTS idx_scores_team_id ON scores(team_id);
CREATE INDEX IF NOT EXISTS idx_scores_round_id ON scores(round_id);

-- =====================================================
-- DONE!
-- =====================================================
