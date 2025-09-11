package com.icdc.insurance.dto;

import lombok.Data;

@Data
public class QuoteRequest {
    private Long userId;
    private Long productId;
    private Long vehicleId;
    private int durationDays; // number of days (frontend can convert months/years to days)
    private Double basePrice; // vehicle base price
    private int vehicleYear;
}
