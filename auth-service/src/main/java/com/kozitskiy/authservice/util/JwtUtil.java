package com.kozitskiy.authservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expire}")
    private long accessExpire;

    @Value("${jwt.refresh-expire}")
    private long refreshExpire;

    @PostConstruct
    public void init() {
        System.out.println("JWT Secret Key: " +
                (secretKey != null ? "***" + secretKey.substring(secretKey.length() - 4) : "NULL"));
        System.out.println("Access expire: " + accessExpire);
        System.out.println("Refresh expire: " + refreshExpire);
    }

    public String generateAccessToken(String email){
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpire))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String generateRefreshToken(String email){
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpire))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public boolean validate(String token){
        try {
            JWT.require(Algorithm.HMAC256(secretKey)).build().verify(token);
            return true;
        }catch (JWTVerificationException e){
            return false;
        }
    }

    public String getEmailFromToken(String token){
        return JWT.decode(token).getSubject();
    }}
