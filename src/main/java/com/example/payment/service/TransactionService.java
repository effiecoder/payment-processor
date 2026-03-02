package com.example.payment.service;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.validation.AmountValidator;
import com.example.payment.validation.DateValidator;
import com.example.payment.validation.IbanValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    public Transaction createTransaction(Map<String, String> request) {
        // Extract main fields
        String amount = request.get("amount");
        String currency = request.get("currency");
        String valueDate = request.get("valueDate");
        
        // Get IBANs (support both old and new field names)
        String senderIban = request.get("debtorAccountIban") != null ? 
            request.get("debtorAccountIban") : request.get("senderAccount");
        String receiverIban = request.get("creditorAccountIban") != null ? 
            request.get("creditorAccountIban") : request.get("receiverAccount");

        // Validate amount
        var amountResult = amountValidator.validate(new BigDecimal(amount));
        if (!amountResult.isValid()) {
            throw new IllegalArgumentException("Invalid amount: " + amountResult.getErrorMessage());
        }

        // Validate sender IBAN
        if (senderIban != null && !senderIban.isEmpty()) {
            var senderResult = ibanValidator.validate(senderIban);
            if (!senderResult.isValid()) {
                throw new IllegalArgumentException("Invalid sender IBAN: " + senderResult.getErrorMessage());
            }
        }

        // Validate receiver IBAN
        if (receiverIban != null && !receiverIban.isEmpty()) {
            var receiverResult = ibanValidator.validate(receiverIban);
            if (!receiverResult.isValid()) {
                throw new IllegalArgumentException("Invalid receiver IBAN: " + receiverResult.getErrorMessage());
            }
        }

        // Validate value date
        var dateResult = dateValidator.validate(LocalDate.parse(valueDate));
        if (!dateResult.isValid()) {
            throw new IllegalArgumentException("Invalid value date: " + dateResult.getErrorMessage());
        }

        // Check for idempotency
        String transactionId = request.get("transactionId");
        String painMessageId = request.get("messageId") != null ? request.get("messageId") : request.get("painMessageId");
        if (transactionId != null && painMessageId != null) {
            if (transactionRepository.existsByTransactionIdAndPainMessageId(transactionId, painMessageId)) {
                throw new IllegalArgumentException("Duplicate transaction: " + transactionId);
            }
        }

        Transaction tx = new Transaction();
        
        // ISO20022 Header
        tx.setMessageId(request.get("messageId") != null ? request.get("messageId") : UUID.randomUUID().toString());
        tx.setMessageType(request.get("messageType") != null ? request.get("messageType") : "pain.001");
        tx.setCreationDateTime(Instant.now());
        
        // Payment Instruction
        tx.setPaymentInstructionId(request.get("paymentInstructionId"));
        tx.setPaymentMethod(request.get("paymentMethod") != null ? request.get("paymentMethod") : "TRN");
        
        if (request.get("batchBooking") != null) {
            tx.setBatchBooking(Boolean.parseBoolean(request.get("batchBooking")));
        }
        
        if (request.get("requestedExecutionDate") != null) {
            tx.setRequestedExecutionDate(LocalDate.parse(request.get("requestedExecutionDate")));
        }
        
        tx.setChargeBearer(request.get("chargeBearer") != null ? request.get("chargeBearer") : "SLEV");
        
        // Amount & Currency
        tx.setAmount(new BigDecimal(amount));
        tx.setCurrency(currency);
        tx.setValueDate(LocalDate.parse(valueDate));
        
        // Instructed Amount (ISO20022)
        tx.setInstructedAmount(new BigDecimal(amount));
        tx.setInstructedAmountCurrency(currency);
        
        // Debtor (Nadawca)
        tx.setDebtorName(request.get("debtorName"));
        tx.setDebtorLegalName(request.get("debtorLegalName"));
        tx.setDebtorAccountIban(senderIban);
        tx.setDebtorAgentBic(request.get("debtorAgentBic"));
        tx.setDebtorAgentName(request.get("debtorAgentName"));
        tx.setDebtorAddressLine(request.get("debtorAddressLine"));
        tx.setDebtorCountry(request.get("debtorCountry"));
        
        // Legacy support
        tx.setSenderName(tx.getDebtorName());
        tx.setSenderAccount(tx.getDebtorAccountIban());
        
        // Creditor (Odbiorca)
        tx.setCreditorName(request.get("creditorName"));
        tx.setCreditorLegalName(request.get("creditorLegalName"));
        tx.setCreditorAccountIban(receiverIban);
        tx.setCreditorAgentBic(request.get("creditorAgentBic"));
        tx.setCreditorAgentName(request.get("creditorAgentName"));
        tx.setCreditorAddressLine(request.get("creditorAddressLine"));
        tx.setCreditorCountry(request.get("creditorCountry"));
        
        // Legacy support
        tx.setReceiverName(tx.getCreditorName());
        tx.setReceiverAccount(tx.getCreditorAccountIban());
        
        // Remittance Information
        tx.setRemittanceUnstructured(request.get("remittanceUnstructured"));
        tx.setRemittanceReference(request.get("remittanceReference"));
        tx.setRemittanceStructuredType(request.get("remittanceStructuredType"));
        tx.setRemittanceStructuredIssuer(request.get("remittanceStructuredIssuer"));
        
        // Legacy support
        tx.setPaymentTitle(tx.getRemittanceUnstructured());
        
        // Purpose
        tx.setPurposeCode(request.get("purposeCode"));
        tx.setPurposeProprietary(request.get("purposeProprietary"));
        
        // Transaction ID
        tx.setTransactionId(transactionId);
        tx.setPainMessageId(painMessageId);
        
        // Ultimate Parties
        tx.setUltimateDebtorName(request.get("ultimateDebtorName"));
        tx.setUltimateDebtorAccountIban(request.get("ultimateDebtorAccountIban"));
        tx.setUltimateCreditorName(request.get("ultimateCreditorName"));
        tx.setUltimateCreditorAccountIban(request.get("ultimateCreditorAccountIban"));
        
        // Initiating Party
        tx.setInitiatingPartyName(request.get("initiatingPartyName"));
        tx.setInitiatingPartyLegalId(request.get("initiatingPartyLegalId"));
        
        // Set initial status
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
            tx.setRemittanceUnstructured(paymentTitle);
        }

        tx.setStatus(TransactionStatus.VALIDATED);
        
        return transactionRepository.save(tx);
    }

    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        
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
