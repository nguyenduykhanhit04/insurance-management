package com.training.insurance.service;

import com.training.insurance.dto.request.InsuranceFormRequest;
import com.training.insurance.dto.request.InsuranceSearchCriteria;
import com.training.insurance.dto.response.InsuranceItemResponse;
import org.springframework.data.domain.Page;

import java.io.PrintWriter;

public interface InsuranceService {

    Page<InsuranceItemResponse> searchInsurances(InsuranceSearchCriteria criteria);

    InsuranceItemResponse getInsuranceDetail(Integer insuranceId);

    InsuranceFormRequest getFormDtoForEdit(Integer insuranceId);

    void createInsurance(InsuranceFormRequest form);

    void updateInsurance(InsuranceFormRequest form);

    void deleteInsurance(Integer insuranceId);

    void exportCsv(InsuranceSearchCriteria criteria, PrintWriter writer);
}
