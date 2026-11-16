package com.achal.jobportal.job.dto;

import com.achal.jobportal.job.entity.Job;
import com.achal.jobportal.job.entity.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// What the API sends back for a job. Only the company's id and name are included, not the whole Company.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String salary;
    private JobType jobType;
    private Long companyId;
    private String companyName;
    private LocalDateTime createdAt;

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getSalary(),
                job.getJobType(),
                job.getCompany().getId(),
                job.getCompany().getName(),
                job.getCreatedAt()
        );
    }
}
