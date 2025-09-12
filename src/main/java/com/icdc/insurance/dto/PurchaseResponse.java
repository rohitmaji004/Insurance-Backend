package com.icdc.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {
    private Long policyId;
    private String policyNo;
    private String status;
    private String startDate;
    private String endDate;
    private Double premium;
    private Double idv;
}
