package com.training.insurance.service.impl;

import com.training.insurance.entity.Company;
import com.training.insurance.exception.AppException;
import com.training.insurance.repository.CompanyRepository;
import com.training.insurance.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAllByOrderByCompanyNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Company getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin công ty!"));
    }

    @Override
    @Transactional
    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }
}
