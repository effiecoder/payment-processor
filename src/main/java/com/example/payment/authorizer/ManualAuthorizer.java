package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ManualAuthorizer implements Authorizer {

    private final BigDecimal manualThreshold;
    private final BigDecimal automaticThreshold;

    public ManualAuthorizer(
            @Value("${authorization.manual-threshold:100000}") BigDecimal manualThreshold,
            @Value("${authorization.automatic-threshold:10000}") BigDecimal automaticThreshold) {
        this.manualThreshold = manualThreshold;
        this.automaticThreshold = automaticThreshold;
    }

    @Override
    public AuthorizationResult authorize(Transaction transaction) {
        // Manual authorization requires human approval
        return AuthorizationResult.PENDING;
    }

    @Override
    public boolean supports(Transaction transaction) {
        if (transaction.getAmount() == null) {
            return false;
        }
        
        BigDecimal amount = transaction.getAmount();
        boolean isMediumAmount = amount.compareTo(automaticThreshold) >= 0 
                && amount.compareTo(manualThreshold) < 0;
        
        // Support medium amounts in PLN
        return isMediumAmount && "PLN".equals(transaction.getCurrency());
    }

    @Override
    public AuthorizerType getType() {
        return AuthorizerType.MANUAL;
    }
}
