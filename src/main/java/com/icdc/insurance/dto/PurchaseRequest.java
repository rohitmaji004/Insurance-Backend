package com.icdc.insurance.dto;

import com.icdc.insurance.validation.ValidExpiryDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidExpiryDate
public class PurchaseRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Vehicle ID cannot be null")
    private Long vehicleId;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private int durationDays;

    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod; // "CARD" or "UPI"

    // Card-specific fields, conditionally required if paymentMethod is "CARD"
    private String cardNumber;
    private String cardHolderName;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cvv;
}
