package com.achal.jobportal.application.entity;

import com.achal.jobportal.candidate.entity.Candidate;
import com.achal.jobportal.job.entity.Job;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        // A candidate can apply to the same job only once.
        uniqueConstraints = @UniqueConstraint(
                name = "uk_applications_job_candidate",
                columnNames = {"job_id", "candidate_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    // Saved as text ("APPLIED") instead of a number, so reordering the enum is safe.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(length = 2000)
    private String coverLetter;

    @Column(length = 1000)
    private String recruiterNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    // Set manually in the service when the recruiter reviews the application.
    private LocalDateTime reviewedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = appliedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
