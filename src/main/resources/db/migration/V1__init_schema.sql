CREATE TABLE jobs (
    id UUID PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    payload TEXT,
    status VARCHAR(50) NOT NULL,
    schedule_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    retry_max_attempts INT DEFAULT 3,
    retry_backoff_seconds INT DEFAULT 30,
    version BIGINT DEFAULT 0
);

CREATE TABLE job_executions (
    id UUID PRIMARY KEY,
    job_id UUID REFERENCES jobs(id),
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    attempt_number INT NOT NULL
);

CREATE INDEX idx_jobs_status_schedule ON jobs(status, schedule_at);
