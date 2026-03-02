package com.example.payment.service;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.validation.AmountValidator;
import com.example.payment.validation.DateValidator;
import com.example.payment.validation.IbanValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuthorizationService authorizationService;
    private final AmountValidator amountValidator;
    private final IbanValidator ibanValidator;
    private final DateValidator dateValidator;

    public TransactionService(
            TransactionRepository transactionRepository,
            AuthorizationService authorizationService,
            AmountValidator amountValidator,
            IbanValidator ibanValidator,
            DateValidator dateValidator) {
        this.transactionRepository = transactionRepository;
        this.authorizationService = authorizationService;
        this.amountValidator = amountValidator;
        this.ibanValidator = ibanValidator;
        this.dateValidator = dateValidator;
    }

    @Transactional
    public Transaction createTransaction(
            String painMessageId,
            String transactionId,
            String amount,
            String currency,
            LocalDate valueDate,
            String senderName,
            String senderAccount,
            String receiverName,
            String receiverAccount,
            String paymentTitle) {

        // Validate amount
        var amountResult = amountValidator.validate(new java.math.BigDecimal(amount));
        if (!amountResult.isValid()) {
            throw new IllegalArgumentException("Invalid amount: " + amountResult.getErrorMessage());
        }

        // Validate sender IBAN
        var senderResult = ibanValidator.validate(senderAccount);
        if (!senderResult.isValid()) {
            throw new IllegalArgumentException("Invalid sender IBAN: " + senderResult.getErrorMessage());
        }

        // Validate receiver IBAN
        var receiverResult = ibanValidator.validate(receiverAccount);
        if (!receiverResult.isValid()) {
            throw new IllegalArgumentException("Invalid receiver IBAN: " + receiverResult.getErrorMessage());
        }

        // Validate value date
        var dateResult = dateValidator.validate(valueDate);
        if (!dateResult.isValid()) {
            throw new IllegalArgumentException("Invalid value date: " + dateResult.getErrorMessage());
        }

        // Validate payment title (basic XSS protection)
        if (paymentTitle != null && paymentTitle.length() > 140) {
            throw new IllegalArgumentException("Payment title too long (max 140 characters)");
        }

        // Check for idempotency
        if (transactionRepository.existsByTransactionIdAndPainMessageId(transactionId, painMessageId)) {
            throw new IllegalArgumentException("Duplicate transaction: " + transactionId);
        }

        Transaction tx = new Transaction();
        tx.setPainMessageId(painMessageId);
        tx.setTransactionId(transactionId);
        tx.setAmount(new java.math.BigDecimal(amount));
        tx.setCurrency(currency);
        tx.setValueDate(valueDate);
        tx.setSenderName(senderName);
        tx.setSenderAccount(senderAccount);
        tx.setReceiverName(receiverName);
        tx.setReceiverAccount(receiverAccount);
        tx.setPaymentTitle(paymentTitle);
        tx.setStatus(TransactionStatus.RECEIVED);

        tx = transactionRepository.save(tx);

        // Auto-authorize
        tx = authorizationService.authorize(tx);

        return tx;
    }

    public List<Transaction> getAllTransactions(int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = transactionRepository.findAll(pageable);
        return pageResult.getContent();
    }

    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Transaction updateTransaction(Long id, String amount, LocalDate valueDate, String paymentTitle) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        if (tx.getStatus() != TransactionStatus.PENDING_APPROVAL && 
            tx.getStatus() != TransactionStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot edit transaction in status: " + tx.getStatus());
        }

        if (amount != null) {
            var result = amountValidator.validate(new java.math.BigDecimal(amount));
            if (!result.isValid()) {
                throw new IllegalArgumentException("Invalid amount: " + result.getErrorMessage());
            }
            tx.setAmount(new java.math.BigDecimal(amount));
        }

        if (valueDate != null) {
            var result = dateValidator.validate(valueDate);
            if (!result.isValid()) {
                throw new IllegalArgumentException("Invalid value date: " + result.getErrorMessage());
            }
            tx.setValueDate(valueDate);
        }

        if (paymentTitle != null) {
            if (paymentTitle.length() > 140) {
                throw new IllegalArgumentException("Payment title too long");
            }
            tx.setPaymentTitle(paymentTitle);
        }

        tx.setStatus(TransactionStatus.VALIDATED);
        
        return transactionRepository.save(tx);
    }

    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        
        // Order matters - main flow
        counts.put("RECEIVED", transactionRepository.countByStatus(TransactionStatus.RECEIVED));
        counts.put("VALIDATED", transactionRepository.countByStatus(TransactionStatus.VALIDATED));
        counts.put("AUTHORIZING", transactionRepository.countByStatus(TransactionStatus.AUTHORIZING));
        counts.put("AUTHORIZED", transactionRepository.countByStatus(TransactionStatus.AUTHORIZED));
        counts.put("PENDING_APPROVAL", transactionRepository.countByStatus(TransactionStatus.PENDING_APPROVAL));
        counts.put("APPROVED", transactionRepository.countByStatus(TransactionStatus.APPROVED));
        counts.put("SENT_TO_CLEARING", transactionRepository.countByStatus(TransactionStatus.SENT_TO_CLEARING));
        counts.put("COMPLETED", transactionRepository.countByStatus(TransactionStatus.COMPLETED));
        
        // Secondary statuses
        counts.put("VALIDATION_FAILED", transactionRepository.countByStatus(TransactionStatus.VALIDATION_FAILED));
        counts.put("AUTHORIZATION_FAILED", transactionRepository.countByStatus(TransactionStatus.AUTHORIZATION_FAILED));
        counts.put("REJECTED", transactionRepository.countByStatus(TransactionStatus.REJECTED));
        counts.put("SUSPENDED", transactionRepository.countByStatus(TransactionStatus.SUSPENDED));
        counts.put("FAILED", transactionRepository.countByStatus(TransactionStatus.FAILED));
        
        return counts;
    }
}
