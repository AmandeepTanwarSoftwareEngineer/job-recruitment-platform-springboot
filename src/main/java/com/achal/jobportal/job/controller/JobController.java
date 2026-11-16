package com.achal.jobportal.job.controller;

import com.achal.jobportal.job.dto.JobRequest;
import com.achal.jobportal.job.dto.JobResponse;
import com.achal.jobportal.job.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // 201 for a recruiter with a company, 400 if they have no company yet or the input is invalid,
    // 403 for a job seeker, 401 with no or a bad token.
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Every job, newest first. Any signed-in user (job seekers browse this to apply).
    @GetMapping
    public List<JobResponse> allJobs() {
        return jobService.getAllJobs();
    }

    // Jobs posted by the logged-in recruiter's company, newest first.
    @GetMapping("/mine")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<JobResponse> myJobs() {
        return jobService.getMyJobs();
    }
}
