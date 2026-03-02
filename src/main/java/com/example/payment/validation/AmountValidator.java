package com.example.payment.validation;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class AmountValidator {

    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.01);
    private static final BigDecimal MAX_DAILY_LIMIT = BigDecimal.valueOf(1_000_000);

    public ValidationResult validate(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.invalid("Amount is required");
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            return ValidationResult.invalid("Amount must be greater than 0");
        }

        if (amount.compareTo(MAX_DAILY_LIMIT) > 0) {
            return ValidationResult.invalid("Amount exceeds daily limit");
        }

        // Check decimal places (max 2)
        BigDecimal[] parts = amount.divideAndRemainder(BigDecimal.ONE);
        int decimalPlaces = parts[1].scale();
        if (decimalPlaces > 2) {
            return ValidationResult.invalid("Amount cannot have more than 2 decimal places");
        }

        return ValidationResult.valid();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
