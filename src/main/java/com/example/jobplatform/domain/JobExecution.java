package com.example.jobplatform.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_executions")
@Data
@NoArgsConstructor
public class JobExecution {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private ZonedDateTime startedAt;
    private ZonedDateTime finishedAt;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private int attemptNumber;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
