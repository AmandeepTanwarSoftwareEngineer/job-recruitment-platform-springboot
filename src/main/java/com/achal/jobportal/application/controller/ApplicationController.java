package com.achal.jobportal.application.controller;

import com.achal.jobportal.application.dto.ApplicationRequest;
import com.achal.jobportal.application.dto.ApplicationResponse;
import com.achal.jobportal.application.dto.ApplicationStatusRequest;
import com.achal.jobportal.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // Apply to a job. 201 on success, 400 if the job seeker has no profile yet or the input is invalid,
    // 404 if the job does not exist, 409 if they already applied, 403 for a recruiter.
    @PostMapping
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.apply(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // The job seeker's own applications, newest first.
    @GetMapping("/mine")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public List<ApplicationResponse> myApplications() {
        return applicationService.getMyApplications();
    }

    // Applications to the recruiter's jobs, newest first.
    @GetMapping("/received")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> receivedApplications() {
        return applicationService.getReceivedApplications();
    }

    // Change the status (and private note) of an application to one of the recruiter's jobs.
    // 200 on success, 400 on invalid input, 404 if it is not one of their applications.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request);
    }
}
