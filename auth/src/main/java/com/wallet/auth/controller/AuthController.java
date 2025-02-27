package com.wallet.auth.controller;

import com.wallet.auth.DTO.JWTToken;
import com.wallet.auth.DTO.LoginRequest;
import com.wallet.auth.DTO.RegisterRequest;
import com.wallet.auth.model.User;
import com.wallet.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication and Authorization Service", description = "Handles user Registration and Authentication")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user account.")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<JWTToken>login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(authService.login(request),HttpStatus.CREATED);
    }

    @Operation(summary = "Get user profile", description = "Returns details of the logged-in user.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(HttpServletRequest request) {
        return new ResponseEntity<>(authService.getProfile(request),HttpStatus.OK);
    }

    @Operation(summary = "Delete user profile", description = "Deletes the logged-in user, wallets and its transactions.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(HttpServletRequest request) {
        return new ResponseEntity<>(authService.delete(request),HttpStatus.OK);
    }


}
