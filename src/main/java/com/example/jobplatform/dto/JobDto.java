package com.example.jobplatform.dto;

import com.example.jobplatform.domain.JobStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

public class JobDto {

    @Data
    public static class CreateJobRequest {
        @NotBlank
        private String type;
        
        private String payload; // JSON string
        
        private ZonedDateTime scheduleAt;
        
        @Valid
        private RetryPolicy retryPolicy;
    }

    @Data
    public static class RetryPolicy {
        @Min(0)
        private int maxRetries = 3;
        
        @Min(1)
        private int backoffSeconds = 30;
    }

    @Data
    public static class JobResponse {
        private UUID id;
        private String type;
        private String payload;
        private JobStatus status;
        private ZonedDateTime scheduleAt;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private int retryMaxAttempts;
        private int retryBackoffSeconds;
    }
}
