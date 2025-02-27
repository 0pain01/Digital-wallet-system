package com.wallet.auth.service;


import com.wallet.auth.DTO.LoginRequest;
import com.wallet.auth.DTO.RegisterRequest;
import com.wallet.auth.DTO.JWTToken;
import com.wallet.auth.exception.FailedAttemptException;
import com.wallet.auth.exception.UserAlreadyExistsException;
import com.wallet.auth.exception.UserNotExistsException;
import com.wallet.auth.exception.WrongCredentialException;
import com.wallet.auth.model.User;
import com.wallet.auth.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserEventPublisher userEventPublisher;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int BLOCK_TIME_MINUTES = 5;

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        userEventPublisher.publishUserCreatedEvent(savedUser);
        return "User registered successfully!";
    }

    public JWTToken login(LoginRequest request) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = "failedAttempts:" + request.getEmail();
        String failedAttempts = ops.get(key);
        if (failedAttempts != null && Integer.parseInt(failedAttempts) >= MAX_FAILED_ATTEMPTS) {
            throw new FailedAttemptException("⚠️ Too many failed attempts! Try again later.");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isEmpty()) {
            throw new UserNotExistsException("User not found!");
        }

        if (!passwordEncoder.matches(request.getPassword(), existingUser.get().getPassword())) {
            throw new WrongCredentialException(handleFailedAttempt(request.getEmail(), ops));
        }
        redisTemplate.delete(key);
        JWTToken token = new JWTToken();
        token.setToken(JWTUtil.generateToken(existingUser.get()));
        return token;
    }

    public User getProfile(HttpServletRequest request) {
        // Extract JWT token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UserNotExistsException("Unauthorized"); // Unauthorized
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userId = JWTUtil.extractUserId(token);

        assert userId != null;
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotExistsException("User not exists!"); // Not found
        }
        return user.get();
    }

    public String delete(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UserNotExistsException("Unauthorized"); // Unauthorized
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String userId = JWTUtil.extractUserId(token);

        assert userId != null;
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotExistsException("User not exists!"); // Not found
        }

        userEventPublisher.publishUserDeletedEvent(user.get()); // Use existing user instead of request body
        userRepository.deleteById(userId);

        return "User deleted successfully!";
    }

    private String handleFailedAttempt(String email, ValueOperations<String, String> ops) {
        String key = "failedAttempts:" + email;
        int attempts = ops.get(key) != null ? Integer.parseInt(Objects.requireNonNull(ops.get(key))) + 1 : 1;
        ops.set(key, String.valueOf(attempts), BLOCK_TIME_MINUTES, TimeUnit.MINUTES);  // Store attempt for X minutes

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            throw new FailedAttemptException("⚠️ Anomaly Detected! Too many failed login attempts.");
        }

        return "Invalid credentials!";
    }
}
