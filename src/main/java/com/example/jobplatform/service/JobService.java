package com.example.jobplatform.service;

import com.example.jobplatform.domain.Job;
import com.example.jobplatform.domain.JobStatus;
import com.example.jobplatform.dto.JobDto;
import com.example.jobplatform.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public JobDto.JobResponse createJob(JobDto.CreateJobRequest request) {
        Job job = new Job();
        job.setType(request.getType());
        job.setPayload(request.getPayload());
        
        if (request.getScheduleAt() != null) {
            job.setScheduleAt(request.getScheduleAt());
        } // else null implies immediate, but we might set it to Now() if we want strict logical "scheduled time"
        
        if (request.getRetryPolicy() != null) {
            job.setRetryMaxAttempts(request.getRetryPolicy().getMaxRetries());
            job.setRetryBackoffSeconds(request.getRetryPolicy().getBackoffSeconds());
        }

        // Default logic: If scheduleAt is null or past/now, it's eligible to run.
        // We set it to PENDING. The Scheduler or Immediate Trigger (if we add one) moves it to QUEUED.
        job.setStatus(JobStatus.PENDING);

        Job savedJob = jobRepository.saveAndFlush(job);
        log.info("Created job: {}", savedJob.getId());

        return mapToResponse(savedJob);
    }

    public List<JobDto.JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JobDto.JobResponse getJob(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id)); // In real app, use custom ResourceNotFoundException
        return mapToResponse(job);
    }

    private JobDto.JobResponse mapToResponse(Job job) {
        JobDto.JobResponse response = new JobDto.JobResponse();
        response.setId(job.getId());
        response.setType(job.getType());
        response.setPayload(job.getPayload());
        response.setStatus(job.getStatus());
        response.setScheduleAt(job.getScheduleAt());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setRetryMaxAttempts(job.getRetryMaxAttempts());
        response.setRetryBackoffSeconds(job.getRetryBackoffSeconds());
        return response;
    }
}
