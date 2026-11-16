package com.achal.jobportal.candidate.dto;

import com.achal.jobportal.candidate.entity.Candidate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// What the API sends back for a candidate. Only safe user fields are included, never the password.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String skills;
    private Integer experienceYears;
    private String currentLocation;
    private String resumeUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Call this inside the service (in the transaction), because user is loaded lazily.
    public static CandidateResponse from(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getUser().getId(),
                candidate.getUser().getName(),
                candidate.getUser().getEmail(),
                candidate.getPhone(),
                candidate.getSkills(),
                candidate.getExperienceYears(),
                candidate.getCurrentLocation(),
                candidate.getResumeUrl(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt()
        );
    }
}
