-- PulseSpeed Database Schema
-- Compatible with SQLite (development) and PostgreSQL (production)

CREATE TABLE IF NOT EXISTS speed_results (
    id SERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    download_mbps DOUBLE PRECISION NOT NULL,
    upload_mbps DOUBLE PRECISION NOT NULL,
    ping_ms INTEGER NOT NULL,
    jitter_ms INTEGER NOT NULL,
    packet_loss DOUBLE PRECISION DEFAULT 0.0,
    server_name VARCHAR(255) NOT NULL,
    server_location VARCHAR(255),
    client_ip VARCHAR(64),
    network_type VARCHAR(64),
    rating_grade VARCHAR(10),
    user_agent TEXT
);

CREATE TABLE IF NOT EXISTS servers (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sponsor VARCHAR(255),
    city VARCHAR(128) NOT NULL,
    country VARCHAR(128) NOT NULL,
    country_code VARCHAR(4),
    host VARCHAR(255) NOT NULL,
    ping_url TEXT,
    download_url TEXT,
    upload_url TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT TRUE
);

-- Indices for rapid query & analytics
CREATE INDEX IF NOT EXISTS idx_speed_results_timestamp ON speed_results(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_speed_results_network ON speed_results(network_type);
