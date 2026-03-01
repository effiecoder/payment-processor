package com.example.payment.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IbanValidator {

    // Country code to IBAN length mapping
    private static final Map<String, Integer> IBAN_LENGTHS = new HashMap<>();
    
    static {
        IBAN_LENGTHS.put("AL", 28);  // Albania
        IBAN_LENGTHS.put("AD", 24);  // Andorra
        IBAN_LENGTHS.put("AT", 20);  // Austria
        IBAN_LENGTHS.put("AZ", 28);  // Azerbaijan
        IBAN_LENGTHS.put("BH", 22);  // Bahrain
        IBAN_LENGTHS.put("BY", 28);  // Belarus
        IBAN_LENGTHS.put("BE", 16);  // Belgium
        IBAN_LENGTHS.put("BA", 20);  // Bosnia and Herzegovina
        IBAN_LENGTHS.put("BR", 29);  // Brazil
        IBAN_LENGTHS.put("BG", 22);  // Bulgaria
        IBAN_LENGTHS.put("CR", 22);  // Costa Rica
        IBAN_LENGTHS.put("HR", 21);  // Croatia
        IBAN_LENGTHS.put("CY", 28);  // Cyprus
        IBAN_LENGTHS.put("CZ", 24);  // Czech Republic
        IBAN_LENGTHS.put("DK", 18);  // Denmark
        IBAN_LENGTHS.put("DO", 28);  // Dominican Republic
        IBAN_LENGTHS.put("TL", 23);  // East Timor
        IBAN_LENGTHS.put("EE", 20);  // Estonia
        IBAN_LENGTHS.put("FO", 18);  // Faroe Islands
        IBAN_LENGTHS.put("FI", 18);  // Finland
        IBAN_LENGTHS.put("FR", 27);  // France
        IBAN_LENGTHS.put("GE", 22);  // Georgia
        IBAN_LENGTHS.put("DE", 22);  // Germany
        IBAN_LENGTHS.put("GI", 23);  // Gibraltar
        IBAN_LENGTHS.put("GR", 27);  // Greece
        IBAN_LENGTHS.put("GL", 18);  // Greenland
        IBAN_LENGTHS.put("GT", 28);  // Guatemala
        IBAN_LENGTHS.put("HU", 28);  // Hungary
        IBAN_LENGTHS.put("IS", 26);  // Iceland
        IBAN_LENGTHS.put("IQ", 23);  // Iraq
        IBAN_LENGTHS.put("IE", 22);  // Ireland
        IBAN_LENGTHS.put("IL", 23);  // Israel
        IBAN_LENGTHS.put("IT", 27);  // Italy
        IBAN_LENGTHS.put("JO", 30);  // Jordan
        IBAN_LENGTHS.put("KZ", 20);  // Kazakhstan
        IBAN_LENGTHS.put("XK", 20);  // Kosovo
        IBAN_LENGTHS.put("KW", 30);  // Kuwait
        IBAN_LENGTHS.put("LV", 21);  // Latvia
        IBAN_LENGTHS.put("LB", 28);  // Lebanon
        IBAN_LENGTHS.put("LI", 21);  // Liechtenstein
        IBAN_LENGTHS.put("LT", 20);  // Lithuania
        IBAN_LENGTHS.put("LU", 20);  // Luxembourg
        IBAN_LENGTHS.put("MK", 19);  // Macedonia
        IBAN_LENGTHS.put("MT", 31);  // Malta
        IBAN_LENGTHS.put("MR", 27);  // Mauritania
        IBAN_LENGTHS.put("MU", 30);  // Mauritius
        IBAN_LENGTHS.put("MC", 27);  // Monaco
        MDAN(28);  // Moldova
        ME(22);    // Montenegro
        NL(18);    // Netherlands
        NO(15);    // Norway
        PK(24);    // Pakistan
        PS(29);    // Palestine
        PL(28);    // Poland
        PT(25);    // Portugal
        QA(29);    // Qatar
        RO(24);    // Romania
        SM(27);    // San Marino
        SA(24);    // Saudi Arabia
        RS(22);    // Serbia
        SK(24);    // Slovakia
        SI(19);    // Slovenia
        ES(24);    // Spain
        SE(24);    // Sweden
        CH(21);    // Switzerland
        TN(24);    // Tunisia
        TR(26);    // Turkey
        UA(29);    // Ukraine
        AE(23);    // United Arab Emirates
        GB(22);    // United Kingdom
        VA(22);    // Vatican City
    }

    private static void MDAN(int length) { IBAN_LENGTHS.put("MD", length); }
    private static void ME(int length) { IBAN_LENGTHS.put("ME", length); }
    private static void NL(int length) { IBAN_LENGTHS.put("NL", length); }
    private static void NO(int length) { IBAN_LENGTHS.put("NO", length); }
    private static void PK(int length) { IBAN_LENGTHS.put("PK", length); }
    private static void PS(int length) { IBAN_LENGTHS.put("PS", length); }
    private static void PL(int length) { IBAN_LENGTHS.put("PL", length); }
    private static void PT(int length) { IBAN_LENGTHS.put("PT", length); }
    private static void QA(int length) { IBAN_LENGTHS.put("QA", length); }
    private static void RO(int length) { IBAN_LENGTHS.put("RO", length); }
    private static void SM(int length) { IBAN_LENGTHS.put("SM", length); }
    private static void SA(int length) { IBAN_LENGTHS.put("SA", length); }
    private static void RS(int length) { IBAN_LENGTHS.put("RS", length); }
    private static void SK(int length) { IBAN_LENGTHS.put("SK", length); }
    private static void SI(int length) { IBAN_LENGTHS.put("SI", length); }
    private static void ES(int length) { IBAN_LENGTHS.put("ES", length); }
    private static void SE(int length) { IBAN_LENGTHS.put("SE", length); }
    private static void CH(int length) { IBAN_LENGTHS.put("CH", length); }
    private static void TN(int length) { IBAN_LENGTHS.put("TN", length); }
    private static void TR(int length) { IBAN_LENGTHS.put("TR", length); }
    private static void UA(int length) { IBAN_LENGTHS.put("UA", length); }
    private static void AE(int length) { IBAN_LENGTHS.put("AE", length); }
    private static void GB(int length) { IBAN_LENGTHS.put("GB", length); }
    private static void VA(int length) { IBAN_LENGTHS.put("VA", length); }

    public ValidationResult validate(String iban) {
        if (iban == null || iban.isBlank()) {
            return ValidationResult.invalid("IBAN is required");
        }

        // Remove spaces and convert to uppercase
        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();

        // Check format: 2 letters + 2 digits + up to 30 alphanumeric
        if (!cleanIban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$")) {
            return ValidationResult.invalid("Invalid IBAN format");
        }

        // Check country code
        String countryCode = cleanIban.substring(0, 2);
        if (!IBAN_LENGTHS.containsKey(countryCode)) {
            return ValidationResult.invalid("Unknown country code: " + countryCode);
        }

        // Check length
        int expectedLength = IBAN_LENGTHS.get(countryCode);
        if (cleanIban.length() != expectedLength) {
            return ValidationResult.invalid("Invalid IBAN length for " + countryCode + ". Expected: " + expectedLength + ", got: " + cleanIban.length());
        }

        // Validate checksum using MOD-97
        if (!isValidMod97(cleanIban)) {
            return ValidationResult.invalid("Invalid IBAN checksum");
        }

        return ValidationResult.valid();
    }

    private boolean isValidMod97(String iban) {
        // Move first 4 chars to end
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        
        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(Character.getNumericValue(c));
            } else {
                numeric.append(c);
            }
        }
        
        // Calculate MOD 97
        String numStr = numeric.toString();
        int[] digits = numStr.chars().map(c -> c - '0').toArray();
        
        int remainder = 0;
        for (int digit : digits) {
            remainder = (remainder * 10 + digit) % 97;
        }
        
        return remainder == 1;
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
