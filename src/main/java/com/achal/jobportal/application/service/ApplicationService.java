package com.achal.jobportal.application.service;

import com.achal.jobportal.application.dto.ApplicationRequest;
import com.achal.jobportal.application.dto.ApplicationResponse;
import com.achal.jobportal.application.dto.ApplicationStatusRequest;
import com.achal.jobportal.application.entity.Application;
import com.achal.jobportal.application.repository.ApplicationRepository;
import com.achal.jobportal.candidate.entity.Candidate;
import com.achal.jobportal.candidate.repository.CandidateRepository;
import com.achal.jobportal.company.repository.CompanyRepository;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.BadRequestException;
import com.achal.jobportal.exception.DuplicateResourceException;
import com.achal.jobportal.exception.ResourceNotFoundException;
import com.achal.jobportal.job.entity.Job;
import com.achal.jobportal.job.repository.JobRepository;
import com.achal.jobportal.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserProvider currentUserProvider;

    public ApplicationService(ApplicationRepository applicationRepository, CandidateRepository candidateRepository,
                              JobRepository jobRepository, CompanyRepository companyRepository,
                              CurrentUserProvider currentUserProvider) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // ---------- Job seeker ----------

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request) {
        // The recruiter sees the candidate's profile, so it must exist before applying.
        User user = currentUserProvider.getCurrentUser();
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Create your profile before applying"));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        // One application per job -> 409 with a clear message.
        // (Two clicks at the same moment are still stopped by the unique key on job_id + candidate_id.)
        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new DuplicateResourceException("You have already applied to this job");
        }

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCoverLetter(request.getCoverLetter());

        Application saved = applicationRepository.save(application);
        return ApplicationResponse.forCandidate(saved);
    }

    // The logged-in job seeker's applications. Empty if they have no profile yet.
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications() {
        User user = currentUserProvider.getCurrentUser();
        return candidateRepository.findByUserId(user.getId())
                .map(candidate -> applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidate.getId()))
                .orElse(List.of())
                .stream()
                .map(ApplicationResponse::forCandidate)
                .toList();
    }

    // ---------- Recruiter ----------

    // Applications to the logged-in recruiter's jobs. Empty if they have no company yet.
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getReceivedApplications() {
        User recruiter = currentUserProvider.getCurrentUser();
        return companyRepository.findByRecruiterId(recruiter.getId())
                .map(company -> applicationRepository.findByJobCompanyIdOrderByAppliedAtDesc(company.getId()))
                .orElse(List.of())
                .stream()
                .map(ApplicationResponse::forRecruiter)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatusRequest request) {
        User recruiter = currentUserProvider.getCurrentUser();

        // Another company's application gets the same 404 as a missing one,
        // so a recruiter cannot find out which application ids exist.
        Application application = applicationRepository.findById(applicationId)
                .filter(found -> found.getJob().getCompany().getRecruiter().getId().equals(recruiter.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        application.setStatus(request.getStatus());
        application.setRecruiterNote(request.getRecruiterNote());
        application.setReviewedAt(LocalDateTime.now());

        // saveAndFlush runs @PreUpdate now, so the response has the new updatedAt.
        Application saved = applicationRepository.saveAndFlush(application);
        return ApplicationResponse.forRecruiter(saved);
    }
}
