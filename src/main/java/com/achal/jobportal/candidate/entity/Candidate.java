package com.achal.jobportal.candidate.entity;

import com.achal.jobportal.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// candidate/entity/Candidate.java
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "candidate_id"}))
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String phone;

    private String headline;

    private String skills;

    private Integer experienceYears;

    private String currentLocation;

    private String highestQualification;

    private String resumeUrl;

    private LocalDateTime createdAt;     // TODO: set automatically on create

    private LocalDateTime updatedAt;     // TODO: set automatically on update
}