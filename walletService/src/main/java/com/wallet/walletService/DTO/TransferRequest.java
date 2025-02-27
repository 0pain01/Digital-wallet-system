package com.wallet.walletService.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferRequest {
    @Schema(example = "John@example.com", description = "Receiver's unique ID")
    @NotBlank(message = "Receiver's email cannot be blank")
    private String receiverEmail;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(example = "12345.00", description = "Amount transferred")
    private BigDecimal amount;
    public TransferRequest(){}
    public TransferRequest(String receiverEmail, BigDecimal amount) {
        this.receiverEmail = receiverEmail;
        this.amount = amount;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
