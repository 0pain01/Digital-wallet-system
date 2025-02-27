package com.wallet.walletService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.wallet.walletService.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
