package com.icdc.insurance.dto;

import lombok.Data;

@Data
public class QuoteRequest {
    private String fullName; // Added to match frontend
    private String vehicleNumber; // Added to match frontend
    private Long userId;
    private Long productId;
    private Long vehicleId;
    private int durationDays;
    private Double basePrice;
    private int vehicleYear;
}