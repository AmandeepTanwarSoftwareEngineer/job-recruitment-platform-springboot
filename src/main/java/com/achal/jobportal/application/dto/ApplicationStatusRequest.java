package com.achal.jobportal.application.dto;

import com.achal.jobportal.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body of PATCH /api/applications/{id}/status (the recruiter reviewing an application).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusRequest {

    // APPLIED, REVIEWED, SHORTLISTED, REJECTED or HIRED. Any other value gets a 400.
    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    // Private to the recruiter; the candidate never sees it.
    @Size(max = 1000, message = "Note must be at most 1000 characters")
    private String recruiterNote;
}
