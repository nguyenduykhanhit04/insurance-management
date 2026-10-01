package com.training.insurance.service;

import com.training.insurance.entity.Company;

import java.util.List;

public interface CompanyService {

    List<Company> getAllCompanies();

    Company getCompanyById(Integer id);

    Company saveCompany(Company company);
}
