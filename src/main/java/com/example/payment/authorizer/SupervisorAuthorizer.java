package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SupervisorAuthorizer implements Authorizer {

    private final BigDecimal supervisorThreshold;

    public SupervisorAuthorizer(@Value("${authorization.manual-threshold:100000}") BigDecimal supervisorThreshold) {
        this.supervisorThreshold = supervisorThreshold;
    }

    @Override
    public AuthorizationResult authorize(Transaction transaction) {
        // Supervisor authorization always requires supervisor approval
        return AuthorizationResult.PENDING;
    }

    @Override
    public boolean supports(Transaction transaction) {
        if (transaction.getAmount() == null) {
            return false;
        }
        
        BigDecimal amount = transaction.getAmount();
        
        // Support high amounts OR foreign currency transactions
        boolean isHighAmount = amount.compareTo(supervisorThreshold) >= 0;
        boolean isForeignCurrency = transaction.getCurrency() != null 
                && !"PLN".equals(transaction.getCurrency());
        
        return isHighAmount || isForeignCurrency;
    }

    @Override
    public AuthorizerType getType() {
        return AuthorizerType.SUPERVISOR;
    }
}
