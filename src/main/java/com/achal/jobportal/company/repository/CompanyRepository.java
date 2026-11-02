package com.achal.jobportal.company.repository;

import com.achal.jobportal.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    List<Company> findByRecruiterId(Long recruiterId);
}
