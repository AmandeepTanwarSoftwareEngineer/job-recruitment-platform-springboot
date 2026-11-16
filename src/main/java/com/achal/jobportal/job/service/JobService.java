package com.achal.jobportal.job.service;

import com.achal.jobportal.company.entity.Company;
import com.achal.jobportal.company.repository.CompanyRepository;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.BadRequestException;
import com.achal.jobportal.job.dto.JobRequest;
import com.achal.jobportal.job.dto.JobResponse;
import com.achal.jobportal.job.entity.Job;
import com.achal.jobportal.job.repository.JobRepository;
import com.achal.jobportal.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final CurrentUserProvider currentUserProvider;

    public JobService(JobRepository jobRepository, CompanyRepository companyRepository,
                      CurrentUserProvider currentUserProvider) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public JobResponse createJob(JobRequest request) {
        // A job is posted under the logged-in recruiter's company, so the company must exist first.
        User recruiter = currentUserProvider.getCurrentUser();
        Company company = companyRepository.findByRecruiterId(recruiter.getId())
                .orElseThrow(() -> new BadRequestException("Create your company before posting a job"));

        Job job = new Job();
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setJobType(request.getJobType());
        job.setCompany(company);

        Job saved = jobRepository.save(job);
        return JobResponse.from(saved);
    }

    // Jobs of the logged-in recruiter's company. Empty if they have no company yet.
    @Transactional(readOnly = true)
    public List<JobResponse> getMyJobs() {
        User recruiter = currentUserProvider.getCurrentUser();
        return companyRepository.findByRecruiterId(recruiter.getId())
                .map(company -> jobRepository.findByCompanyIdOrderByCreatedAtDesc(company.getId()))
                .orElse(List.of())
                .stream()
                .map(JobResponse::from)
                .toList();
    }

    // All jobs from all companies, newest first. Job seekers browse this list to apply.
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(JobResponse::from)
                .toList();
    }
}
