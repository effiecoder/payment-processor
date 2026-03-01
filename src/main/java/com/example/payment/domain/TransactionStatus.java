package com.example.payment.domain;

public enum TransactionStatus {
    RECEIVED,
    VALIDATED,
    VALIDATION_FAILED,
    AUTHORIZING,
    AUTHORIZED,
    AUTHORIZATION_FAILED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED,
    SENT_TO_CLEARING,
    COMPLETED,
    FAILED
}
