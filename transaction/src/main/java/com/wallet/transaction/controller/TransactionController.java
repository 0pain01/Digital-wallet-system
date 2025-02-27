package com.wallet.transaction.controller;

import com.wallet.transaction.model.Transaction;
import com.wallet.transaction.service.JWTUtil;
import com.wallet.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transaction Service", description = "Handles transaction operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    public TransactionService transactionService;

    @GetMapping("/history")
    @Operation(summary = "Get transaction history", description = "Returns all transactions for a user.")
    public ResponseEntity<?>history(@RequestHeader("Authorization") String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userId = JWTUtil.extractUserId(token); // Extract MongoDB _id from JWT
        String emailId = JWTUtil.extractEmail(token);
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/category/{type}")
    @Operation(summary = "Get transactions by type", description = "Fetch transactions by type of transaction( DEPOSIT,WITHDRAW,TRANSFER )")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String type) {
        type = type.toUpperCase();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String userId = JWTUtil.extractUserId(token);

        List<Transaction> transactions = transactionService.getTransactionsByCategory(userId, type);
        return ResponseEntity.ok(transactions);
    }
}
