package com.icdc.insurance.validation;

import java.time.YearMonth;

import com.icdc.insurance.dto.PurchaseRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator class for the ValidExpiryDate annotation.
 * It checks if the provided card expiry month and year are in the future.
 */
public class ExpiryDateValidator implements ConstraintValidator<ValidExpiryDate, PurchaseRequest> {

    @Override
    public void initialize(ValidExpiryDate constraintAnnotation) {
        // No special initialization required
    }

    @Override
    public boolean isValid(PurchaseRequest request, ConstraintValidatorContext context) {
        // Only validate if the payment method is "CARD"
        if (!"CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            return true;
        }

        // Return true if month or year are null, as @NotNull handles this
        if (request.getExpiryMonth() == null || request.getExpiryYear() == null) {
            return true;
        }

        try {
            // Create a YearMonth object from the request data
            YearMonth expiryDate = YearMonth.of(request.getExpiryYear(), request.getExpiryMonth());
            YearMonth currentDate = YearMonth.now();

            // Check if the expiry date is before the current date
            if (expiryDate.isBefore(currentDate)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Expiry date must be in the future.")
                        .addConstraintViolation();
                return false;
            }
            return true;
        } catch (Exception e) {
            // This catches cases like an invalid month number (e.g., 13)
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid expiry month or year.").addConstraintViolation();
            return false;
        }
    }
}
