package com.wallet.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class FailedAttemptException extends  RuntimeException{
    public FailedAttemptException(String message) {
        super(message);
    }
}
