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
public class QuoteResponse {
    private Long productId;
    private Long vehicleId;
    private Double premium;
    private Double idv;
    private LocalDate startDate;
    private LocalDate endDate;
}
