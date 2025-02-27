package com.wallet.walletService.service;

import com.wallet.walletService.model.Transaction;
import com.wallet.walletService.model.User;
import com.wallet.walletService.model.Wallet;
import com.wallet.walletService.repository.TransactionRepository;
import com.wallet.walletService.repository.UserRepository;
import com.wallet.walletService.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

@Component
public class UserEventSubscriber implements StreamListener<String, MapRecord<String, String, String>> {
    private static final String STREAM_NAME = "user-events";
    private static final String CONSUMER_GROUP = "user-event-group";
    private static final String CONSUMER_NAME = "user-event-consumer";

    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public UserEventSubscriber(StringRedisTemplate redisTemplate, RedisConnectionFactory connectionFactory, UserRepository userRepository, WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        initializeConsumerGroup();
    }

    @PostConstruct
    private void startListening() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, MapRecord>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .executor(Executors.newFixedThreadPool(2))
                        .pollTimeout(Duration.ofSeconds(2))
                        .targetType(MapRecord.class)
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(connectionFactory);

        listenerContainer.receiveAutoAck(
                Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()),
                this
        );

        listenerContainer.start();
    }

    private void initializeConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_NAME, ReadOffset.from("0"), CONSUMER_GROUP);
            System.out.println("Consumer group created: " + CONSUMER_GROUP);
        } catch (Exception e) {
            System.out.println("Consumer group might already exist: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> data = message.getValue();
        String userId = data.get("userId");
        String email = data.get("email");
        String name = data.get("name");
        String eventType = data.get("eventType");

        if(eventType.equals("CREATE")){
            System.out.println("User-create event received => UserId: " + userId + ", Name: "+name +", Email: " + email);
            if (userId != null && email != null) {
                User user = new User(userId, name,email);
                userRepository.save(user);
                System.out.println("User saved to MongoDB successfully");
            } else {
                System.out.println("Invalid data received. Skipping save.");
            }
        }else if(eventType.equals("DELETE")){
            System.out.println("User-delete event received => UserId: " + userId + ", Name: "+name +", Email: " + email);
            if (userId != null && email != null) {
                userRepository.deleteById(userId);
                System.out.println("User deleted from Wallet DB");

                Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Wallet not found for the deleted User"));
                walletRepository.deleteById(wallet.getId());
                System.out.println("Wallet deleted for UserId: " + userId);

                transactionRepository.deleteBySenderIdOrReceiverId(wallet.getUserId(), wallet.getUserId());
                System.out.println("Transactions deleted for UserId: " + userId);

            } else {
                System.out.println("Invalid data received. Skipping Delete.");
            }
        }



        // Ensure userId and email are not null before saving

        // Acknowledge the message
        redisTemplate.opsForStream().acknowledge(CONSUMER_GROUP, message);
    }
}
