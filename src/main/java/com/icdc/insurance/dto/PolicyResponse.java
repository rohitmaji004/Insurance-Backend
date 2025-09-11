package com.icdc.insurance.dto;

import java.time.LocalDate;

import com.icdc.insurance.model.Policy; // <-- ADD THIS

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyResponse {
    private Long policyId;
    private String policyNo;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double premium;
    private Double idv;
    private String status;

    public static PolicyResponse fromEntity(Policy policy) {
        return PolicyResponse.builder()
                .policyId(policy.getId())
                .policyNo(policy.getPolicyNo())
                .productName(policy.getProduct().getName())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .premium(policy.getPremium())
                .idv(policy.getVehicle().getIdv())
                .status(policy.getStatus())
                .build();
    }
}
