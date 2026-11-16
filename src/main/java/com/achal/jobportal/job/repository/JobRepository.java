package com.achal.jobportal.job.repository;

import com.achal.jobportal.job.entity.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    // Newest first.
    List<Job> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    // Every job, newest first. The company is loaded in the same query (one query, not one per job).
    @EntityGraph(attributePaths = "company")
    List<Job> findAllByOrderByCreatedAtDesc();
}
