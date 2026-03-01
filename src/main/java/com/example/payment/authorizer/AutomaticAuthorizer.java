package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AutomaticAuthorizer implements Authorizer {

    private final BigDecimal threshold;

    public AutomaticAuthorizer(@Value("${authorization.automatic-threshold:10000}") BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public AuthorizationResult authorize(Transaction transaction) {
        // Automatic authorization for low-risk transactions
        return AuthorizationResult.APPROVED;
    }

    @Override
    public boolean supports(Transaction transaction) {
        // Support if amount is below threshold and currency is PLN
        if (transaction.getAmount() == null) {
            return false;
        }
        
        boolean belowThreshold = transaction.getAmount().compareTo(threshold) < 0;
        boolean domesticCurrency = "PLN".equals(transaction.getCurrency());
        
        return belowThreshold && domesticCurrency;
    }

    @Override
    public AuthorizerType getType() {
        return AuthorizerType.AUTOMATIC;
    }
}
