package com.example.jobplatform.worker;

import com.example.jobplatform.domain.Job;
import com.example.jobplatform.domain.JobExecution;
import com.example.jobplatform.domain.JobStatus;
import com.example.jobplatform.repository.JobExecutionRepository;
import com.example.jobplatform.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessor {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;

    @Transactional
    public void processJob(UUID jobId) {
        log.info("Processing job: {}", jobId);
        
        // 1. Fetch Job with small retry
        Job job = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            job = jobRepository.findById(jobId).orElse(null);
            if (job != null) break;
            log.warn("Job {} not found in DB on attempt {}. Retrying...", jobId, attempt + 1);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }

        if (job == null) {
            log.error("Job {} NOT FOUND after retries.", jobId);
            return;
        }

        // 2. Idempotency Check
        if (job.getStatus() != JobStatus.QUEUED && job.getStatus() != JobStatus.PENDING && job.getStatus() != JobStatus.RUNNING) {
            log.warn("Job {} is in state {}. Skipping.", jobId, job.getStatus());
            return;
        }

        JobExecution execution = new JobExecution();
        try {
            // 3. Status Record
            execution.setJob(job);
            execution.setStatus(JobStatus.RUNNING);
            execution.setStartedAt(ZonedDateTime.now());
            execution.setAttemptNumber(1); // Simplification
            jobExecutionRepository.save(execution);

            job.setStatus(JobStatus.RUNNING);
            job.setUpdatedAt(ZonedDateTime.now());
            jobRepository.save(job);

            // 4. Logic
            executeJobLogic(job);

            // Success
            job.setStatus(JobStatus.COMPLETED);
            job.setUpdatedAt(ZonedDateTime.now());
            jobRepository.save(job);

            execution.setStatus(JobStatus.COMPLETED);
            execution.setFinishedAt(ZonedDateTime.now());
            jobExecutionRepository.save(execution);
            log.info("Job {} completed successfully.", jobId);

        } catch (Exception e) {
            log.error("Job {} failed: {}", jobId, e.getMessage());
            handleFailure(job, execution, e);
        }
    }

    private void executeJobLogic(Job job) throws Exception {
        if ("ALWAYS_FAIL".equals(job.getType())) {
            throw new RuntimeException("Persistent Failure");
        }
        Thread.sleep(1000); // Simulate work
    }

    private void handleFailure(Job job, JobExecution execution, Exception e) {
        execution.setStatus(JobStatus.FAILED);
        execution.setErrorMessage(e.getMessage());
        execution.setFinishedAt(ZonedDateTime.now());
        jobExecutionRepository.save(execution);

        long failureCount = jobExecutionRepository.countByJobId(job.getId());
        if (failureCount <= job.getRetryMaxAttempts()) {
            job.setStatus(JobStatus.PENDING);
            int backoff = job.getRetryBackoffSeconds() * (int) Math.pow(2, failureCount - 1);
            job.setScheduleAt(ZonedDateTime.now().plusSeconds(backoff));
            log.info("Job {} retrying in {}s", job.getId(), backoff);
        } else {
            job.setStatus(JobStatus.FAILED);
            log.error("Job {} failed permanently", job.getId());
        }
        job.setUpdatedAt(ZonedDateTime.now());
        jobRepository.save(job);
    }
}
