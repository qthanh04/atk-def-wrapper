-- ==============================================================
-- ATK-DEF Backend Database Schema
-- PostgreSQL DDL Script
-- Schema: adg_core (unified schema)
-- ==============================================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS adg_core;

-- ==============================================================
-- 1. TEAMS TABLE (Login + Team Info)
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.teams (
    id              SERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'TEAM',
    name            VARCHAR(100) NOT NULL,
    affiliation     VARCHAR(200),
    country         VARCHAR(50),
    ip_address      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for teams
CREATE INDEX IF NOT EXISTS idx_teams_username ON adg_core.teams(username);
CREATE INDEX IF NOT EXISTS idx_teams_name ON adg_core.teams(name);

COMMENT ON TABLE adg_core.teams IS 'Team accounts - each team has one login account';
COMMENT ON COLUMN adg_core.teams.username IS 'Login username';
COMMENT ON COLUMN adg_core.teams.password IS 'BCrypt hashed password';
COMMENT ON COLUMN adg_core.teams.role IS 'ADMIN or TEAM';
COMMENT ON COLUMN adg_core.teams.name IS 'Team display name';

-- ==============================================================
-- 2. GAMES TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.games (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(100) NOT NULL UNIQUE,
    description             TEXT,
    vulnbox_path            VARCHAR(500),
    checker_module          VARCHAR(200),
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    tick_duration_seconds   INTEGER DEFAULT 60,
    current_tick            INTEGER DEFAULT 0,
    start_time              TIMESTAMP,
    end_time                TIMESTAMP,
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for games
CREATE INDEX IF NOT EXISTS idx_games_status ON adg_core.games(status);
CREATE INDEX IF NOT EXISTS idx_games_name ON adg_core.games(name);

COMMENT ON TABLE adg_core.games IS 'Attack-Defense game instances';
COMMENT ON COLUMN adg_core.games.status IS 'DRAFT, DEPLOYING, RUNNING, PAUSED, FINISHED';
COMMENT ON COLUMN adg_core.games.tick_duration_seconds IS 'Duration of each tick in seconds';

-- ==============================================================
-- 3. GAME_TEAMS TABLE (Team participation in games)
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.game_teams (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id         UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    team_id         INTEGER NOT NULL REFERENCES adg_core.teams(id) ON DELETE CASCADE,
    container_name  VARCHAR(200),
    container_ip    VARCHAR(50),
    ssh_username    VARCHAR(50),
    ssh_password    VARCHAR(100),
    ssh_port        INTEGER,
    token           VARCHAR(64) NOT NULL UNIQUE,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_id, team_id)
);

-- Indexes for game_teams
CREATE INDEX IF NOT EXISTS idx_game_teams_game_id ON adg_core.game_teams(game_id);
CREATE INDEX IF NOT EXISTS idx_game_teams_team_id ON adg_core.game_teams(team_id);
CREATE INDEX IF NOT EXISTS idx_game_teams_token ON adg_core.game_teams(token);

COMMENT ON TABLE adg_core.game_teams IS 'Team participation in games with container/SSH info';
COMMENT ON COLUMN adg_core.game_teams.token IS 'Token for flag submission';

-- ==============================================================
-- 4. TICKS TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.ticks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id         UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    tick_number     INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    flags_placed    INTEGER DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_id, tick_number)
);

-- Indexes for ticks
CREATE INDEX IF NOT EXISTS idx_ticks_game_id ON adg_core.ticks(game_id);
CREATE INDEX IF NOT EXISTS idx_ticks_status ON adg_core.ticks(status);

COMMENT ON TABLE adg_core.ticks IS 'Game ticks (time periods)';
COMMENT ON COLUMN adg_core.ticks.status IS 'PENDING, RUNNING, COMPLETED';

-- ==============================================================
-- 5. FLAGS TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.flags (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id         UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    team_id         VARCHAR(100) NOT NULL,
    tick_id         UUID NOT NULL REFERENCES adg_core.ticks(id) ON DELETE CASCADE,
    flag_type       VARCHAR(20) NOT NULL DEFAULT 'SERVICE',
    flag_value      VARCHAR(128) NOT NULL UNIQUE,
    valid_until     TIMESTAMP NOT NULL,
    is_stolen       BOOLEAN NOT NULL DEFAULT FALSE,
    stolen_count    INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_id, team_id, tick_id, flag_type)
);

-- Indexes for flags
CREATE INDEX IF NOT EXISTS idx_flags_game_id ON adg_core.flags(game_id);
CREATE INDEX IF NOT EXISTS idx_flags_team_id ON adg_core.flags(team_id);
CREATE INDEX IF NOT EXISTS idx_flags_flag_value ON adg_core.flags(flag_value);

COMMENT ON TABLE adg_core.flags IS 'Flags placed on team vulnboxes each tick';
COMMENT ON COLUMN adg_core.flags.flag_type IS 'SERVICE or BONUS';

-- ==============================================================
-- 6. FLAG_SUBMISSIONS TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.flag_submissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id             UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    attacker_team_id    VARCHAR(100) NOT NULL,
    flag_id             UUID REFERENCES adg_core.flags(id) ON DELETE SET NULL,
    submitted_flag      VARCHAR(128) NOT NULL,
    status              VARCHAR(20) NOT NULL,
    points              INTEGER DEFAULT 0,
    submitted_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for flag_submissions
CREATE INDEX IF NOT EXISTS idx_flag_submissions_game_id ON adg_core.flag_submissions(game_id);
CREATE INDEX IF NOT EXISTS idx_flag_submissions_attacker ON adg_core.flag_submissions(attacker_team_id);
CREATE INDEX IF NOT EXISTS idx_flag_submissions_status ON adg_core.flag_submissions(status);

COMMENT ON TABLE adg_core.flag_submissions IS 'Flag submission attempts by attackers';
COMMENT ON COLUMN adg_core.flag_submissions.status IS 'ACCEPTED, REJECTED, DUPLICATE, EXPIRED, OWN_FLAG, INVALID';

-- ==============================================================
-- 7. SCOREBOARD TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.scoreboard (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id         UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    team_id         VARCHAR(100) NOT NULL,
    attack_points   INTEGER DEFAULT 0,
    defense_points  INTEGER DEFAULT 0,
    sla_points      INTEGER DEFAULT 0,
    total_points    INTEGER DEFAULT 0,
    rank            INTEGER DEFAULT 0,
    flags_captured  INTEGER DEFAULT 0,
    flags_lost      INTEGER DEFAULT 0,
    last_updated    TIMESTAMP,
    UNIQUE(game_id, team_id)
);

-- Indexes for scoreboard
CREATE INDEX IF NOT EXISTS idx_scoreboard_game_id ON adg_core.scoreboard(game_id);
CREATE INDEX IF NOT EXISTS idx_scoreboard_rank ON adg_core.scoreboard(game_id, rank);

COMMENT ON TABLE adg_core.scoreboard IS 'Live scoreboard for each game';

-- ==============================================================
-- 8. SERVICE_STATUSES TABLE
-- ==============================================================
CREATE TABLE IF NOT EXISTS adg_core.service_statuses (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id             UUID NOT NULL REFERENCES adg_core.games(id) ON DELETE CASCADE,
    team_id             VARCHAR(100) NOT NULL,
    tick_id             UUID NOT NULL REFERENCES adg_core.ticks(id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL,
    sla_percentage      REAL NOT NULL DEFAULT 100.0,
    error_message       TEXT,
    check_duration_ms   INTEGER,
    checked_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(game_id, team_id, tick_id)
);

-- Indexes for service_statuses
CREATE INDEX IF NOT EXISTS idx_service_statuses_game_id ON adg_core.service_statuses(game_id);
CREATE INDEX IF NOT EXISTS idx_service_statuses_status ON adg_core.service_statuses(status);

COMMENT ON TABLE adg_core.service_statuses IS 'Service checker results per tick';
COMMENT ON COLUMN adg_core.service_statuses.status IS 'OK, DOWN, MUMBLE, CORRUPT, ERROR';

-- ==============================================================
-- INITIAL DATA: Create Admin Account
-- Password: admin123 (BCrypt hash)
-- ==============================================================
INSERT INTO adg_core.teams (username, password, role, name)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3YJHgkIvPh2DPFT.Nqje', 'ADMIN', 'Administrator')
ON CONFLICT (username) DO NOTHING;

-- ==============================================================
-- SUMMARY: All tables in adg_core schema
-- ==============================================================
-- 1. teams           - Team login + info
-- 2. games           - Game instances
-- 3. game_teams      - Team participation + container info
-- 4. ticks           - Time periods
-- 5. flags           - Flags on vulnboxes
-- 6. flag_submissions - Attacker submissions
-- 7. scoreboard      - Live scores
-- 8. service_statuses - SLA checker results
-- ==============================================================
