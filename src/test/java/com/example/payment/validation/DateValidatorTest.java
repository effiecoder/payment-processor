package com.example.payment.validation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateValidatorTest {

    private final DateValidator validator = new DateValidator();

    @Test
    void validate_todayDate_returnsValid() {
        var result = validator.validate(LocalDate.now());
        assertTrue(result.isValid());
    }

    @Test
    void validate_futureDate_returnsValid() {
        var result = validator.validate(LocalDate.now().plusDays(30));
        assertTrue(result.isValid());
    }

    @Test
    void validate_pastDate_returnsInvalid() {
        var result = validator.validate(LocalDate.now().minusDays(1));
        assertFalse(result.isValid());
        assertEquals("Value date cannot be in the past", result.getErrorMessage());
    }

    @Test
    void validate_nullDate_returnsInvalid() {
        var result = validator.validate(null);
        assertFalse(result.isValid());
        assertEquals("Value date is required", result.getErrorMessage());
    }

    @Test
    void validate_dateTooFarInFuture_returnsInvalid() {
        var result = validator.validate(LocalDate.now().plusYears(2));
        assertFalse(result.isValid());
    }

    @Test
    void validate_nearFutureDate_returnsValid() {
        var result = validator.validate(LocalDate.now().plusDays(100));
        assertTrue(result.isValid());
    }
}
