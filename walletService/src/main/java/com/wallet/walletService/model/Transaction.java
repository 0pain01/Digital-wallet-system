package com.wallet.walletService.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    @Schema(example = "67ba9a233c69a91a23xxxxxx", description = "Sender's unique ID")
    private String senderId;
    @Schema(example = "67ba9a233c69a91a23xxxxxx", description = "Receiver's unique ID")
    private String receiverId;

    @Schema(example = "12345.00", description = "Amount transferred")
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;
    @Schema(example = "DEPOSIT", description = "DEPOSIT/ WITHDRAW/ TRANSFER")
    private String type; // DEPOSIT, WITHDRAW, TRANSFER
    @Schema(example = "2025-02-23T03:47:41.972+00:00", description = "Date-Time of Transaction")
    private LocalDateTime timestamp;

    public Transaction(){}

    public Transaction(String id, String senderId, String receiverId, BigDecimal amount, String type, LocalDateTime timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
