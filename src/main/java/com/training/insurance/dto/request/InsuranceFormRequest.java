package com.training.insurance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceFormRequest {

    private Integer id;
    private Integer userId;

    @Builder.Default
    private String companyType = "EXIST";

    private Integer companyId;

    private String newCompanyName;
    private String newAddress;
    private String newEmail;
    private String newTelephone;

    private String username;
    private String password;
    private String userFullName;
    private String userSexDivision;
    private String birthdate;

    private String insuranceNumber;
    private String startDate;
    private String endDate;
    private String placeOfRegister;
}
