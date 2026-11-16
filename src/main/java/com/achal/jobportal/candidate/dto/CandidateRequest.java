package com.achal.jobportal.candidate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body of PUT /api/candidates/me. The user is not sent here: it is the logged-in user.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRequest {

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @NotBlank(message = "Skills are required")
    @Size(max = 1000, message = "Skills must be at most 1000 characters")
    private String skills;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience must be at most 50 years")
    private Integer experienceYears;

    @Size(max = 100, message = "Location must be at most 100 characters")
    private String currentLocation;

    // Recruiters open this as a link, so only allow http(s) links (no "javascript:..." tricks). Empty is fine.
    @Size(max = 500, message = "Resume URL must be at most 500 characters")
    @Pattern(regexp = "^(https?://\\S+)?$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Resume URL must start with http:// or https://")
    private String resumeUrl;
}
