package com.wallet.transaction.service;

import com.wallet.transaction.model.Transaction;
import com.wallet.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {


    @Autowired
    private TransactionRepository transactionRepository;

    // Get all transactions where the user is involved (as sender or receiver)
    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    // Get transactions by category (type) where user is sender or receiver
    public List<Transaction> getTransactionsByCategory(String userId, String category) {
        if (category.equals("TRANSFER")) {
            List<Transaction> transactions = transactionRepository.findBySenderIdAndType(userId, category);
            transactions.addAll(transactionRepository.findByReceiverIdAndType(userId, category));
            return transactions;
        }
        return transactionRepository.findBySenderIdAndType(userId, category);
    }
}

