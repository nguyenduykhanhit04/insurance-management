package com.training.insurance.controller;

import com.training.insurance.dto.response.CompanyResponse;
import com.training.insurance.entity.Company;
import com.training.insurance.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyApiController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyDetails(@PathVariable Integer id) {
        Company company = companyService.getCompanyById(id);
        CompanyResponse res = CompanyResponse.builder()
                .companyInternalId(company.getCompanyInternalId())
                .companyName(company.getCompanyName())
                .address(company.getAddress())
                .email(company.getEmail())
                .telephone(company.getTelephone())
                .build();
        return ResponseEntity.ok(res);
    }
}
