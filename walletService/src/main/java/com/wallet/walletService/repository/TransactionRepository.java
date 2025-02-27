package com.wallet.walletService.repository;

import com.wallet.walletService.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction,String> {
    List<Transaction> findBySenderIdOrReceiverId(String senderId, String receiverId);

    void deleteBySenderIdOrReceiverId(String senderId, String receiverId);
}
