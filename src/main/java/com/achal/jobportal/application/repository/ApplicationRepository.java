package com.achal.jobportal.application.repository;

import com.achal.jobportal.application.entity.Application;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    // @EntityGraph loads the job, company, candidate and user in the same query,
    // instead of one extra query per application when the response is built.

    // A job seeker's applications, newest first.
    @EntityGraph(attributePaths = {"job", "job.company", "candidate", "candidate.user"})
    List<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    // Applications to any job of one company (job -> company -> id), newest first.
    @EntityGraph(attributePaths = {"job", "job.company", "candidate", "candidate.user"})
    List<Application> findByJobCompanyIdOrderByAppliedAtDesc(Long companyId);
}
