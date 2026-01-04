package com.example.jobplatform.repository;

import com.example.jobplatform.domain.Job;
import com.example.jobplatform.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND (j.scheduleAt IS NULL OR j.scheduleAt <= :now)")
    List<Job> findReadyJobs(@Param("status") JobStatus status, @Param("now") ZonedDateTime now);

    @Query("SELECT j FROM Job j WHERE (j.status = 'QUEUED' OR j.status = 'RUNNING') AND j.updatedAt <= :threshold")
    List<Job> findStaleJobs(@Param("threshold") ZonedDateTime threshold);
}
