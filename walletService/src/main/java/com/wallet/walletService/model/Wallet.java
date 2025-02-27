package com.wallet.walletService.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;


@Document(collection = "wallets")
public class Wallet {

    @Schema(hidden = true)
    @Id
    private String id;

    @Schema(example = "67ba9a233c69a91a23xxxxxx", description = "User's unique ID")
    private String userId;
    @Schema(example = "John@example.com", description = "User's emailId")
    private String email;

    @Schema(example = "12345.000", description = "User's wallet balance")
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance;

    @Schema(example = "INR", description = "User's country currency code (INR)")
    private String currency;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public Wallet(String userId, String id,String email, BigDecimal balance,String currency) {
        this.userId = userId;
        this.id = id;
        this.email=email;
        this.balance = balance;
        this.currency=currency;
    }

    public Wallet(){

    }
}
