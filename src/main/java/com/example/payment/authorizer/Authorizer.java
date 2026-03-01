package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;

public interface Authorizer {
    
    AuthorizationResult authorize(Transaction transaction);
    
    boolean supports(Transaction transaction);
    
    AuthorizerType getType();
    
    enum AuthorizationResult {
        APPROVED,
        REJECTED,
        PENDING
    }
    
    enum AuthorizerType {
        AUTOMATIC,
        MANUAL,
        SUPERVISOR
    }
}
