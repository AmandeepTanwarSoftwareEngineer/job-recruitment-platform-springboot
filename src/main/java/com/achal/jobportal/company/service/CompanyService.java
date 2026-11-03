package com.achal.jobportal.company.service;

import com.achal.jobportal.company.dto.CompanyRequest;
import com.achal.jobportal.company.dto.CompanyResponse;
import com.achal.jobportal.company.entity.Company;
import com.achal.jobportal.company.repository.CompanyRepository;
import com.achal.jobportal.entity.User;
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

        Company company = new Company();
        company.setName(request.getName().trim());
        company.setDescription(request.getDescription());
        company.setLocation(request.getLocation());
        company.setRecruiter(recruiter);

        Company saved = companyRepository.save(company);
        return CompanyResponse.from(saved);
    }
}
