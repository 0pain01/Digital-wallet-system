package com.wallet.walletService.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WalletBalance {
    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.00", message = "Amount must be a number/decimal")
    @Schema(example = "12345.00", description = "Wallet Balance")
    private BigDecimal balance;
    @NotBlank(message = "Try with INR only")
    @Schema(example = "INR", description = "User's country currency code (INR)")
    private String currency;
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency(){
        return currency;
    }

    public void setCurrency(String currency){
        this.currency=currency;
    }

    public WalletBalance(BigDecimal balance, String currency) {
        this.balance = balance;
        this.currency = currency;
    }

    public WalletBalance(){}
}
