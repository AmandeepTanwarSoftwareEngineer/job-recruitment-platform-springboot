package com.achal.jobportal.candidate.controller;

import com.achal.jobportal.candidate.dto.CandidateRequest;
import com.achal.jobportal.candidate.dto.CandidateResponse;
import com.achal.jobportal.candidate.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    // 200 with the job seeker's profile, 404 if they have not created one yet, 403 for a recruiter.
    @GetMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public CandidateResponse myProfile() {
        return candidateService.getMyProfile();
    }

    // Create or update the profile. 200 with the saved profile, 400 on invalid input, 403 for a recruiter.
    @PutMapping("/me")
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public CandidateResponse saveMyProfile(@Valid @RequestBody CandidateRequest request) {
        return candidateService.saveMyProfile(request);
    }
}
