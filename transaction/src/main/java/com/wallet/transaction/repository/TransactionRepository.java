package com.wallet.transaction.repository;

import com.wallet.transaction.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySenderIdOrReceiverId(String senderId, String receiverId);
    // Fetch transactions by category (type) where the user is the sender or receiver
    List<Transaction> findBySenderIdAndType(String senderId, String type);
    List<Transaction> findByReceiverIdAndType(String receiverId, String type);


}
