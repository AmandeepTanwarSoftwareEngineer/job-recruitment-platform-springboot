package com.achal.jobportal.job.dto;

import com.achal.jobportal.job.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Body of POST /api/jobs. The company is not sent here: it is the logged-in recruiter's company.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @Size(max = 255, message = "Location must be at most 255 characters")
    private String location;

    @Size(max = 50, message = "Salary must be at most 50 characters")
    private String salary;

    // FULL_TIME, PART_TIME, INTERNSHIP or CONTRACT. Any other value gets a 400.
    @NotNull(message = "Job type is required")
    private JobType jobType;
}
