package com.tui.proof.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
public class JwtConfiguration
{
    @Value(value = "${pilotes.secret.key}")
    private @Getter
    String secretKey;

    @Value(value = "${pilotes.jwt.expirationSeconds}")
    private @Getter
    int jwtExpiration;

}
