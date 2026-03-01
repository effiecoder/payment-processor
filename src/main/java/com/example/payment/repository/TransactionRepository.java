package com.example.payment.repository;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByStatus(TransactionStatus status);
    
    Page<Transaction> findByStatusIn(List<TransactionStatus> statuses, Pageable pageable);
    
    Page<Transaction> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    boolean existsByTransactionIdAndPainMessageId(String transactionId, String painMessageId);
    
    long countByStatus(TransactionStatus status);
}
