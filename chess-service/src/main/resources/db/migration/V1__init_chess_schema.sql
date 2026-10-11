-- V1__init_chess_schema.sql

CREATE TABLE IF NOT EXISTS player_stats (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL,
    elo INT NOT NULL DEFAULT 1200,
    peak_elo INT NOT NULL DEFAULT 1200,
    games_played INT NOT NULL DEFAULT 0,
    wins INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_player_stats_user_id ON player_stats (user_id);
CREATE INDEX IF NOT EXISTS idx_player_stats_elo ON player_stats (elo DESC);
