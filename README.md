<img width="1607" height="884" alt="Screenshot (36)" src="https://github.com/user-attachments/assets/e3b48d54-862d-45ae-b10c-139040e0e7f5" />


# Scalable Background Job Processing Platform

A robust, self-healing background job processing engine built with Spring Boot, PostgreSQL, and Redis. This platform allows you to create, schedule, and monitor long-running tasks via a real-time dashboard.

## 🚀 One-Click Start
1.  **Requirement**: Ensure Docker Desktop is running.
2.  **Run**: Double-click `start_platform.bat`.
    - This will start the PostgreSQL/Redis infrastructure and launch the Java application.
3.  **Monitor**: Open [http://localhost:8081](http://localhost:8081).

---

## 🏗️ Architecture
The system follows a producer-consumer pattern with atomic state management:

1.  **Job API**: REST endpoints to create and track jobs.
2.  **Scheduler**: Polls the DB for `PENDING` jobs and pushes them to Redis using a **Transactional Post-Commit Hook**.
3.  **Redis Queue**: Acts as a high-speed broker for job delivery.
4.  **Job Worker**: A multi-threaded pool that pops IDs from Redis and executes them using the `JobProcessor`.
5.  **Self-Healing**: A recovery task that automatically rescues "stale" jobs (orphaned during restarts) and resets them for execution.

---

## 🧪 Sample Payloads

Copy these JSON objects into the **"Payload"** field in the UI modal or use them in your API client:

### 1. Immediate Success (`REPORT_GENERATION`)
```json
{
  "type": "REPORT_GENERATION",
  "payload": "{\"userId\": 123, \"format\": \"PDF\"}",
  "retryPolicy": {
    "maxRetries": 3,
    "backoffSeconds": 10
  }
}
```

### 2. Scheduled Job (`DATA_SYNC`)
Schedule for a specific time (ISO 8601 UTC).
```json
{
  "type": "DATA_SYNC",
  "payload": "{\"source\": \"S3\", \"target\": \"BigQuery\"}",
  "scheduleAt": "2026-01-05T12:00:00Z"
}
```

### 3. Failure & Retry Test (`ALWAYS_FAIL`)
This job will fail and trigger exponential backoff retries.
```json
{
  "type": "ALWAYS_FAIL",
  "payload": "{\"debug\": true}",
  "retryPolicy": {
    "maxRetries": 5,
    "backoffSeconds": 2
  }
}
```

---

## 🛠️ Key Components
- **`JobRepository`**: Custom JPQL queries to find "ready" and "stale" jobs.
- **`JobProcessor`**: Encapsulates `@Transactional` logic to ensure DB state is always consistent.
- **`WebConfig`**: Explicitly maps the dark-mode Admin Dashboard UI.
- **`start_platform.bat`**: Integrated script that handles environment checks and startup.

## 📊 Dashboard States
- `PENDING`: Waiting for its scheduled time.
- `QUEUED`: Pushed to Redis, waiting for a worker.
- `RUNNING`: Currently being processed by a worker thread.
- `COMPLETED`: Finished successfully.
- `FAILED`: Exhausted all retries and moved to permanent failure.
