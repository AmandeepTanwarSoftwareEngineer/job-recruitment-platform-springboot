package com.achal.jobportal.company.dto;

import com.achal.jobportal.company.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// What the API sends back for a company. Only the recruiter's id is included, not the whole User.
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private Long recruiterId;

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getLocation(),
                company.getRecruiter().getId()
        );
    }
}
