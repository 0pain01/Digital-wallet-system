package com.wallet.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class RegisterRequest {
    @NotBlank(message = "Name cannot be blank")
    @Schema(example = "John Doe", description = "User's full name")
    private String name;

    @Schema(example = "john.doe@example.com", description = "User's email address")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Schema(example = "password@123", description = "User's password (must be at least 8 characters)")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RegisterRequest( String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public RegisterRequest(){}

}
