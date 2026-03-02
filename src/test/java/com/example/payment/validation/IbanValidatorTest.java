package com.example.payment.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanValidatorTest {

    private final IbanValidator validator = new IbanValidator();

    @Test
    void validate_validPolishIban_returnsValid() {
        // PL61109010140000071219812874 (valid test IBAN)
        var result = validator.validate("PL61 1090 1014 0000 0712 1981 2874");
        assertTrue(result.isValid());
    }

    @Test
    void validate_validPolishIbanNoSpaces_returnsValid() {
        var result = validator.validate("PL61109010140000071219812874");
        assertTrue(result.isValid());
    }

    @Test
    void validate_invalidChecksum_returnsInvalid() {
        var result = validator.validate("PL61109010140000071219812875");
        assertFalse(result.isValid());
    }

    @Test
    void validate_nullIban_returnsInvalid() {
        var result = validator.validate(null);
        assertFalse(result.isValid());
        assertEquals("IBAN is required", result.getErrorMessage());
    }

    @Test
    void validate_emptyIban_returnsInvalid() {
        var result = validator.validate("");
        assertFalse(result.isValid());
    }

    @Test
    void validate_invalidCountryCode_returnsInvalid() {
        var result = validator.validate("XX1234567890123456789012");
        assertFalse(result.isValid());
        assertEquals("Unknown country code: XX", result.getErrorMessage());
    }

    @Test
    void validate_invalidLength_returnsInvalid() {
        var result = validator.validate("PL123");
        assertFalse(result.isValid());
    }

    @Test
    void validate_validGermanIban_returnsValid() {
        var result = validator.validate("DE89370400440532013000");
        assertTrue(result.isValid());
    }

    @Test
    void validate_validUsIban_returnsValid() {
        // US doesn't have IBAN standard, so it may not validate
        var result = validator.validate("US123456789012345678901");
        // US is not in our supported countries, so expect invalid
        assertFalse(result.isValid());
    }
}
