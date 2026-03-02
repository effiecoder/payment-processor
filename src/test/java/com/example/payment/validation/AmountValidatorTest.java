package com.example.payment.validation;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {

    private final AmountValidator validator = new AmountValidator();

    @Test
    void validate_validAmount_returnsValid() {
        var result = validator.validate(BigDecimal.valueOf(100.00));
        assertTrue(result.isValid());
    }

    @Test
    void validate_zeroAmount_returnsInvalid() {
        var result = validator.validate(BigDecimal.ZERO);
        assertFalse(result.isValid());
        assertEquals("Amount must be greater than 0", result.getErrorMessage());
    }

    @Test
    void validate_negativeAmount_returnsInvalid() {
        var result = validator.validate(BigDecimal.valueOf(-50));
        assertFalse(result.isValid());
    }

    @Test
    void validate_nullAmount_returnsInvalid() {
        var result = validator.validate(null);
        assertFalse(result.isValid());
        assertEquals("Amount is required", result.getErrorMessage());
    }

    @Test
    void validate_exceedsDailyLimit_returnsInvalid() {
        var result = validator.validate(BigDecimal.valueOf(2_000_000));
        assertFalse(result.isValid());
        assertEquals("Amount exceeds daily limit", result.getErrorMessage());
    }

    @Test
    void validate_moreThanTwoDecimalPlaces_returnsInvalid() {
        var result = validator.validate(BigDecimal.valueOf(100.999));
        assertFalse(result.isValid());
    }

    @Test
    void validate_exactTwoDecimalPlaces_returnsValid() {
        var result = validator.validate(BigDecimal.valueOf(100.55));
        assertTrue(result.isValid());
    }
}
