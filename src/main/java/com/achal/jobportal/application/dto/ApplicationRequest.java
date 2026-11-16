package com.achal.jobportal.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body of POST /api/applications (a candidate applying to a job).
// The candidate is not sent here: it is the logged-in user's candidate profile.
// Status (APPLIED), appliedAt and updatedAt are set automatically.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotNull(message = "Job id is required")
    private Long jobId;

    @Size(max = 2000, message = "Cover letter must be at most 2000 characters")
    private String coverLetter;
}
