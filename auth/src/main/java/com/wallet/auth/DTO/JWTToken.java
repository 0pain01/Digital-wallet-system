package com.wallet.auth.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

public class JWTToken {
    @Schema(example = "tken generated" ,description="JWT token is stored here")
    private String token;

    public JWTToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public JWTToken(){};
}
