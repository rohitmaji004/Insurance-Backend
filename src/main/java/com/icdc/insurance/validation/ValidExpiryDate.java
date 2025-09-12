package com.icdc.insurance.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom validation annotation to ensure a credit card expiry date is in the
 * future.
 * The validation logic is implemented in the ExpiryDateValidator class.
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExpiryDateValidator.class)
public @interface ValidExpiryDate {
    String message() default "Expiry date must be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Marker interface for grouping the validation to be applied specifically for
     * card payments.
     */
    interface CardPayment {
    }
}
