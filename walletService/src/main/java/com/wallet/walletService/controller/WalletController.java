package com.wallet.walletService.controller;

import com.wallet.walletService.DTO.TransactionRequest;
import com.wallet.walletService.DTO.TransferRequest;
import com.wallet.walletService.model.Transaction;
import com.wallet.walletService.model.Wallet;
import com.wallet.walletService.DTO.WalletBalance;
import com.wallet.walletService.service.JWTUtil;
import com.wallet.walletService.service.WalletService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Wallet Service", description = "Handles wallet operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    @Operation(summary = "Create Wallet", description = "Creates a new wallet for the user.")
    public ResponseEntity<Wallet> createWallet(@RequestHeader("Authorization") String authHeader,@Valid @RequestBody WalletBalance walletRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userId = JWTUtil.extractUserId(token); // Extract MongoDB _id from JWT
        String emailId = JWTUtil.extractEmail(token);
        Wallet wallet = walletService.createWallet(userId, emailId, walletRequest.getBalance(), walletRequest.getCurrency());
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/balance")
    @Operation(summary = "Check Balance", description = "Check balance in the wallet.")
    public ResponseEntity<?> getWallet(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userId = JWTUtil.extractUserId(token); // Extract MongoDB _id from JWT
        String emailId = JWTUtil.extractEmail(token);
        Wallet wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet.getBalance());
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit Money", description = "Deposits money into the wallet.")
    public ResponseEntity<Wallet> deposit(@RequestHeader("Authorization") String authHeader,@Valid @RequestBody TransactionRequest request) {
        String userId = JWTUtil.extractUserId(authHeader.substring(7));
        Wallet updatedWallet = walletService.deposit(userId, request.getAmount());
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw Money", description = "Withdraw money from the wallet.")
    public ResponseEntity<Wallet> withdraw(@RequestHeader("Authorization") String authHeader,@Valid @RequestBody TransactionRequest request) {
        String userId = JWTUtil.extractUserId(authHeader.substring(7));
        Wallet updatedWallet = walletService.withdraw(userId, request.getAmount());
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer Money", description = "Transfers money to another wallet.")
    public ResponseEntity<Transaction> transfer(@RequestHeader("Authorization") String authHeader,@Valid @RequestBody TransferRequest request) {
        String userId = JWTUtil.extractUserId(authHeader.substring(7));
        Transaction transfer = walletService.transfer(userId,request.getReceiverEmail(),request.getAmount());
        return ResponseEntity.ok(transfer);
    }
}
