package com.training.insurance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceItemResponse {

    private Integer userId;
    private Integer insuranceId;
    private Integer companyId;
    private String companyName;
    private String username;
    private String userFullName;
    private String userSexDivision;
    private String gender; // Nam hoặc Nữ
    private LocalDate birthdate;
    private String insuranceNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String placeOfRegister;
}
