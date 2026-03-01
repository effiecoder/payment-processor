package com.example.payment.controller;

import com.example.payment.domain.Transaction;
import com.example.payment.domain.TransactionStatus;
import com.example.payment.service.AuthorizationService;
import com.example.payment.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthorizationService authorizationService;

    public TransactionController(TransactionService transactionService, AuthorizationService authorizationService) {
        this.transactionService = transactionService;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, String> request) {
        try {
            Transaction tx = transactionService.createTransaction(
                    request.get("painMessageId"),
                    request.get("transactionId"),
                    request.get("amount"),
                    request.get("currency"),
                    LocalDate.parse(request.get("valueDate")),
                    request.get("senderName"),
                    request.get("senderAccount"),
                    request.get("receiverName"),
                    request.get("receiverAccount"),
                    request.get("paymentTitle")
            );
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        Transaction tx = transactionService.getTransaction(id);
        if (tx == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tx);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        try {
            String amount = updates.containsKey("amount") ? updates.get("amount").toString() : null;
            LocalDate valueDate = updates.containsKey("valueDate") 
                    ? LocalDate.parse(updates.get("valueDate").toString()) : null;
            String paymentTitle = updates.containsKey("paymentTitle") 
                    ? updates.get("paymentTitle").toString() : null;

            Transaction tx = transactionService.updateTransaction(id, amount, valueDate, paymentTitle);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveTransaction(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            Transaction tx = authorizationService.approve(id, username);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectTransaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String reason = request.get("reason");
            String username = authentication.getName();
            Transaction tx = authorizationService.reject(id, reason, username);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspendTransaction(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            Transaction tx = authorizationService.suspend(id, username);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resumeTransaction(@PathVariable Long id) {
        try {
            Transaction tx = authorizationService.resume(id);
            return ResponseEntity.ok(tx);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getByStatus(@PathVariable TransactionStatus status) {
        return ResponseEntity.ok(transactionService.getAllTransactions(0, 1000).stream()
                .filter(tx -> tx.getStatus() == status)
                .toList());
    }
}
