package com.achal.jobportal.company.repository;

import com.achal.jobportal.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    // One recruiter has at most one company (unique recruiter_id).
    Optional<Company> findByRecruiterId(Long recruiterId);

    boolean existsByRecruiterId(Long recruiterId);
}
