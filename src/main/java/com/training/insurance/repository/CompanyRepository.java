package com.training.insurance.repository;

import com.training.insurance.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    List<Company> findAllByOrderByCompanyNameAsc();

    Optional<Company> findByCompanyName(String companyName);
}
