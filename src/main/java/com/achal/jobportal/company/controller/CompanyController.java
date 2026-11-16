package com.achal.jobportal.company.controller;

import com.achal.jobportal.company.dto.CompanyRequest;
import com.achal.jobportal.company.dto.CompanyResponse;
import com.achal.jobportal.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // 201 for a recruiter, 403 for a job seeker, 401 with no or a bad token, 400 on invalid input.
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 200 with the recruiter's company, 404 if they have not created one yet.
    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public CompanyResponse myCompany() {
        return companyService.getMyCompany();
    }
}
