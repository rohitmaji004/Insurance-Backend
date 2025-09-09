package com.icdc.insurance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long productId;

    @NotNull
    private Long vehicleId;

    @NotNull
    private int durationDays; // policy length in days

    @NotNull
    private String paymentMethod; // "UPI", "CARD", "DEBIT", "PAYLATER"
}
