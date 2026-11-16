package com.achal.jobportal.application.dto;

import com.achal.jobportal.application.entity.Application;
import com.achal.jobportal.application.entity.ApplicationStatus;
import com.achal.jobportal.candidate.entity.Candidate;
import com.achal.jobportal.job.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// What the API sends back for an application: the job, the candidate and the status.
// Build it inside the service (in the transaction), because job and candidate are loaded lazily.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private ApplicationStatus status;
    private String coverLetter;
    private String recruiterNote;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;

    private Long jobId;
    private String jobTitle;
    private String companyName;

    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateSkills;
    private Integer candidateExperienceYears;
    private String candidateLocation;
    private String candidateResumeUrl;

    // For the recruiter: everything, including their private note.
    public static ApplicationResponse forRecruiter(Application application) {
        return build(application, application.getRecruiterNote());
    }

    // For the candidate: the same, but without the recruiter's private note.
    public static ApplicationResponse forCandidate(Application application) {
        return build(application, null);
    }

    private static ApplicationResponse build(Application application, String recruiterNote) {
        Job job = application.getJob();
        Candidate candidate = application.getCandidate();
        return new ApplicationResponse(
                application.getId(),
                application.getStatus(),
                application.getCoverLetter(),
                recruiterNote,
                application.getAppliedAt(),
                application.getReviewedAt(),
                application.getUpdatedAt(),
                job.getId(),
                job.getTitle(),
                job.getCompany().getName(),
                candidate.getId(),
                candidate.getUser().getName(),
                candidate.getUser().getEmail(),
                candidate.getPhone(),
                candidate.getSkills(),
                candidate.getExperienceYears(),
                candidate.getCurrentLocation(),
                candidate.getResumeUrl()
        );
    }
}
