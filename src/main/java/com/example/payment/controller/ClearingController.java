package com.example.payment.controller;

import com.example.payment.clearing.ClearingService;
import com.example.payment.domain.Transaction;
import com.example.payment.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clearing")
public class ClearingController {

    private final ClearingService clearingService;
    private final TransactionRepository transactionRepository;

    public ClearingController(ClearingService clearingService, TransactionRepository transactionRepository) {
        this.clearingService = clearingService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendToClearing() {
        try {
            String xml = clearingService.sendToClearing();
            if (xml == null) {
                return ResponseEntity.ok(Map.of("message", "No transactions ready for clearing"));
            }
            return ResponseEntity.ok(Map.of(
                "message", "Transactions sent to clearing",
                "count", transactionRepository.findByStatusIn(
                    List.of(
                        com.example.payment.domain.TransactionStatus.AUTHORIZED,
                        com.example.payment.domain.TransactionStatus.APPROVED
                    )
                ).size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send/{id}")
    public ResponseEntity<?> sendSingleTransaction(@PathVariable Long id) {
        try {
            String xml = clearingService.sendSingleTransaction(id);
            return ResponseEntity.ok(Map.of(
                "message", "Transaction sent to clearing",
                "pacs008", xml
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<?> previewClearing() {
        List<Transaction> readyTransactions = transactionRepository.findByStatusIn(
            List.of(
                com.example.payment.domain.TransactionStatus.AUTHORIZED,
                com.example.payment.domain.TransactionStatus.APPROVED
            )
        );
        
        String xml = clearingService.generatePacs008Preview(readyTransactions);
        
        return ResponseEntity.ok(Map.of(
            "count", readyTransactions.size(),
            "pacs008", xml
        ));
    }
}
