package com.training.insurance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {

    private Integer companyInternalId;
    private String companyName;
    private String address;
    private String email;
    private String telephone;
}
