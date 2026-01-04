package com.example.jobplatform.scheduler;

import com.example.jobplatform.domain.Job;
import com.example.jobplatform.domain.JobStatus;
import com.example.jobplatform.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

    private final JobRepository jobRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String QUEUE_NAME = "job_queue";

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @Transactional
    public void scheduleJobs() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Job> readyJobs = jobRepository.findReadyJobs(JobStatus.PENDING, now);

        if (!readyJobs.isEmpty()) {
            log.info("Found {} jobs ready for execution.", readyJobs.size());
            for (Job job : readyJobs) {
                try {
                    // Update Status in DB first
                    job.setStatus(JobStatus.QUEUED);
                    job.setUpdatedAt(now);
                    jobRepository.save(job);

                    // Push to Redis ONLY after successful commit to prevent race conditions
                    org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                redisTemplate.opsForList().leftPush(QUEUE_NAME, job.getId().toString());
                                log.info("Queued job to Redis after commit: {}", job.getId());
                            }
                        }
                    );
                    
                    log.info("Job {} marked as QUEUED in DB, awaiting commit for Redis push.", job.getId());
                } catch (Exception e) {
                    log.error("Failed to prepare job for queueing: {}", job.getId(), e);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 60000) // Run recovery every minute
    @Transactional
    public void recoverStaleJobs() {
        ZonedDateTime threshold = ZonedDateTime.now().minusMinutes(5);
        List<Job> staleJobs = jobRepository.findStaleJobs(threshold);

        if (!staleJobs.isEmpty()) {
            log.info("Found {} stale jobs to recover.", staleJobs.size());
            for (Job job : staleJobs) {
                log.info("Recovering job {}: resetting from {} to PENDING", job.getId(), job.getStatus());
                job.setStatus(JobStatus.PENDING);
                job.setUpdatedAt(ZonedDateTime.now());
                jobRepository.save(job);
            }
        }
    }
}
