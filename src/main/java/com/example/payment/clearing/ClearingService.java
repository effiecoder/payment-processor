package com.example.payment.clearing;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import com.example.payment.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClearingService {

    private static final Logger logger = LoggerFactory.getLogger(ClearingService.class);

    private final TransactionRepository transactionRepository;
    private final Pacs008Generator pacs008Generator;

    public ClearingService(TransactionRepository transactionRepository, Pacs008Generator pacs008Generator) {
        this.transactionRepository = transactionRepository;
        this.pacs008Generator = pacs008Generator;
    }

    @Transactional
    public String sendToClearing() {
        // Get all transactions ready for clearing
        List<Transaction> readyTransactions = transactionRepository.findByStatusIn(
            List.of(TransactionStatus.AUTHORIZED, TransactionStatus.APPROVED)
        );

        if (readyTransactions.isEmpty()) {
            logger.info("No transactions ready for clearing");
            return null;
        }

        logger.info("Processing {} transactions for clearing", readyTransactions.size());

        // Generate pacs.008 XML
        String pacs008Xml = pacs008Generator.generatePacs008(readyTransactions);

        // In a real system, this would send to the clearing system
        // For now, we just log it and update status
        logger.debug("Generated pacs.008: {}", pacs008Xml);

        // Update transaction statuses
        for (Transaction tx : readyTransactions) {
            tx.setStatus(TransactionStatus.SENT_TO_CLEARING);
            transactionRepository.save(tx);
        }

        logger.info("Successfully sent {} transactions to clearing", readyTransactions.size());

        return pacs008Xml;
    }

    @Transactional
    public String sendSingleTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.AUTHORIZED && 
            transaction.getStatus() != TransactionStatus.APPROVED) {
            throw new IllegalStateException("Transaction is not ready for clearing. Current status: " + transaction.getStatus());
        }

        String pacs008Xml = pacs008Generator.generatePacs008(List.of(transaction));

        logger.debug("Generated pacs.008 for transaction {}: {}", transactionId, pacs008Xml);

        transaction.setStatus(TransactionStatus.SENT_TO_CLEARING);
        transactionRepository.save(transaction);

        logger.info("Successfully sent transaction {} to clearing", transactionId);

        return pacs008Xml;
    }

    public String generatePacs008Preview(List<Transaction> transactions) {
        return pacs008Generator.generatePacs008(transactions);
    }
}
