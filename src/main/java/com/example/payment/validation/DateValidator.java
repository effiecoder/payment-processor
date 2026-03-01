package com.example.payment.validation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateValidator {

    private static final long MAX_DAYS_IN_FUTURE = 365;

    public ValidationResult validate(LocalDate valueDate) {
        if (valueDate == null) {
            return ValidationResult.invalid("Value date is required");
        }

        LocalDate today = LocalDate.now();

        // Check if value date is in the past
        if (valueDate.isBefore(today)) {
            return ValidationResult.invalid("Value date cannot be in the past");
        }

        // Check if value date is too far in the future
        long daysBetween = ChronoUnit.DAYS.between(today, valueDate);
        if (daysBetween > MAX_DAYS_IN_FUTURE) {
            return ValidationResult.invalid("Value date cannot be more than " + MAX_DAYS_IN_FUTURE + " days in the future");
        }

        return ValidationResult.valid();
    }

    public record ValidationResult(boolean valid, String errorMessage) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
