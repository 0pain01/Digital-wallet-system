package com.wallet.walletService.service;

import com.wallet.walletService.exception.InsufficientBalanceException;
import com.wallet.walletService.exception.UserNotExistsException;
import com.wallet.walletService.exception.WalletNotExistException;
import com.wallet.walletService.model.Transaction;
import com.wallet.walletService.model.User;
import com.wallet.walletService.model.Wallet;
import com.wallet.walletService.repository.TransactionRepository;
import com.wallet.walletService.repository.UserRepository;
import com.wallet.walletService.repository.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionRepository transactionRepository;


    public Wallet createWallet(String userId, String email, BigDecimal Balance, String Currency) {
        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);

        if (existingWallet.isPresent()) {
            return existingWallet.get(); // Return existing wallet if already created
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(Balance);
        wallet.setEmail(email);
        wallet.setCurrency(Currency);
        return walletRepository.save(wallet);
    }

    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotExistException("Wallet not found"));
    }

    public Wallet deposit(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotExistException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        transactionRepository.save(new Transaction(null, userId, userId, amount, "DEPOSIT", LocalDateTime.now()));
        return wallet;
    }

    public Wallet withdraw(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotExistException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        transactionRepository.save(new Transaction(null, userId, userId, amount, "WITHDRAW", LocalDateTime.now()));
        return wallet;
    }

    public Transaction transfer(String senderId, String receiverEmail, BigDecimal amount) {
        Wallet senderWallet = walletRepository.findByUserId(senderId)
                .orElseThrow(() -> new WalletNotExistException("Sender wallet not found"));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

       User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new UserNotExistsException("Receiver not found"));

       Wallet receiverWallet = walletRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new WalletNotExistException("Receiver wallet not found"));

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction transaction = new Transaction(null, senderId, receiver.getId(), amount, "TRANSFER", LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

}
