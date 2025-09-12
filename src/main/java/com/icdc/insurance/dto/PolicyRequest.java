package com.icdc.insurance.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyRequest {
    private Long userId;
    private Long productId;
    private Long vehicleId; // ✅ added properly
    private int durationDays; // ✅ added properly
    private LocalDate startDate;
    private LocalDate endDate;
    private Double premium;
}
