package com.example.jobplatform.repository;

import com.example.jobplatform.domain.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    
    long countByJobId(UUID jobId);
}
