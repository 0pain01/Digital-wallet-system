package com.wallet.walletService.exception;

public class WalletNotExistException extends RuntimeException {
    public WalletNotExistException(String message) {
        super(message);
    }
}
