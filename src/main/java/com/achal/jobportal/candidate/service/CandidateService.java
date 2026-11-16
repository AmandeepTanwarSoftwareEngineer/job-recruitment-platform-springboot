package com.achal.jobportal.candidate.service;

import com.achal.jobportal.candidate.dto.CandidateRequest;
import com.achal.jobportal.candidate.dto.CandidateResponse;
import com.achal.jobportal.candidate.entity.Candidate;
import com.achal.jobportal.candidate.repository.CandidateRepository;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.ResourceNotFoundException;
import com.achal.jobportal.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CurrentUserProvider currentUserProvider;

    public CandidateService(CandidateRepository candidateRepository, CurrentUserProvider currentUserProvider) {
        this.candidateRepository = candidateRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // The logged-in job seeker's profile, or 404 if they have not created one yet.
    @Transactional(readOnly = true)
    public CandidateResponse getMyProfile() {
        User user = currentUserProvider.getCurrentUser();
        return candidateRepository.findByUserId(user.getId())
                .map(CandidateResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("You have not created your profile yet"));
    }

    // Creates the profile the first time, updates it after that. One user = one profile.
    @Transactional
    public CandidateResponse saveMyProfile(CandidateRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Candidate created = new Candidate();
                    created.setUser(user);
                    return created;
                });

        candidate.setPhone(request.getPhone());
        candidate.setSkills(request.getSkills().trim());
        candidate.setExperienceYears(request.getExperienceYears());
        candidate.setCurrentLocation(request.getCurrentLocation());
        candidate.setResumeUrl(request.getResumeUrl());

        // saveAndFlush runs @PreUpdate now, so the response has the new updatedAt.
        Candidate saved = candidateRepository.saveAndFlush(candidate);
        return CandidateResponse.from(saved);
    }
}
