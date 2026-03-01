package com.example.payment.service;

import com.example.payment.authorizer.*;
import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import com.example.payment.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizationService {

    private final TransactionRepository transactionRepository;
    private final AuthorizerSelector authorizerSelector;

    public AuthorizationService(TransactionRepository transactionRepository, AuthorizerSelector authorizerSelector) {
        this.transactionRepository = transactionRepository;
        this.authorizerSelector = authorizerSelector;
    }

    @Transactional
    public Transaction authorize(Transaction transaction) {
        Authorizer authorizer = authorizerSelector.select(transaction);
        Authorizer.AuthorizationResult result = authorizer.authorize(transaction);

        Authorizer.AuthorizerType type = authorizer.getType();

        switch (result) {
            case APPROVED:
                transaction.setStatus(TransactionStatus.AUTHORIZED);
                transaction.setAuthorizedBy(type.name());
                break;
            case REJECTED:
                transaction.setStatus(TransactionStatus.AUTHORIZATION_FAILED);
                transaction.setAuthorizedBy(type.name());
                break;
            case PENDING:
            default:
                transaction.setStatus(TransactionStatus.PENDING_APPROVAL);
                transaction.setAuthorizedBy(type.name());
                break;
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction approve(Long transactionId, String approverUsername) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Transaction is not pending approval");
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setApprovedBy(approverUsername);
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction reject(Long transactionId, String reason, String rejectorUsername) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() == TransactionStatus.COMPLETED ||
            transaction.getStatus() == TransactionStatus.FAILED) {
            throw new IllegalStateException("Cannot reject completed transaction");
        }

        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setRejectionReason(reason);
        transaction.setApprovedBy(rejectorUsername);
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction suspend(Long transactionId, String suspendorUsername) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() == TransactionStatus.COMPLETED ||
            transaction.getStatus() == TransactionStatus.FAILED) {
            throw new IllegalStateException("Cannot suspend completed transaction");
        }

        transaction.setStatus(TransactionStatus.SUSPENDED);
        
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction resume(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.SUSPENDED) {
            throw new IllegalStateException("Transaction is not suspended");
        }

        // Return to pending approval status
        transaction.setStatus(TransactionStatus.PENDING_APPROVAL);
        
        return transactionRepository.save(transaction);
    }
}
