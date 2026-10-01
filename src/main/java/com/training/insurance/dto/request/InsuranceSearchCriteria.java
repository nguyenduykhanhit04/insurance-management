package com.training.insurance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceSearchCriteria {

    private Integer companyId;
    private String userFullName;
    private String insuranceNumber;
    private String placeOfRegister;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 5;

    @Builder.Default
    private String order = "ASC";
}
