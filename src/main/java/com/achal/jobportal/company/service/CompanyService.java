package com.achal.jobportal.company.service;

import com.achal.jobportal.company.dto.CompanyRequest;
import com.achal.jobportal.company.dto.CompanyResponse;
import com.achal.jobportal.company.entity.Company;
import com.achal.jobportal.company.repository.CompanyRepository;
import com.achal.jobportal.entity.User;
import com.achal.jobportal.exception.DuplicateResourceException;
import com.achal.jobportal.exception.ResourceNotFoundException;
import com.achal.jobportal.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CurrentUserProvider currentUserProvider;

    public CompanyService(CompanyRepository companyRepository, CurrentUserProvider currentUserProvider) {
        this.companyRepository = companyRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        // The logged-in recruiter owns the company; this fills companies.recruiter_id.
        User recruiter = currentUserProvider.getCurrentUser();

        // A recruiter can own only one company -> 409 with a clear message.
        if (companyRepository.existsByRecruiterId(recruiter.getId())) {
            throw new DuplicateResourceException("You already have a company");
        }

        Company company = new Company();
        company.setName(request.getName().trim());
        company.setDescription(request.getDescription());
        company.setLocation(request.getLocation());
        company.setRecruiter(recruiter);

        Company saved = companyRepository.save(company);
        return CompanyResponse.from(saved);
    }

    // The logged-in recruiter's company, or 404 if they have not created one yet.
    @Transactional(readOnly = true)
    public CompanyResponse getMyCompany() {
        User recruiter = currentUserProvider.getCurrentUser();
        return companyRepository.findByRecruiterId(recruiter.getId())
                .map(CompanyResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("You have not created a company yet"));
    }
}
