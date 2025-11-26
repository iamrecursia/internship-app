package com.kozitskiy.authservice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_ACCESS_EXPIRE}")
    private long accessExpire;

    @Value("${JWT_REFRESH_EXPIRE}")
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
        }catch (Exception e){
            return false;
        }
    }

    public String getEmailFromToken(String token){
        return JWT.decode(token).getSubject();
    }


}
//access
//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJrb3ppdHNraXkuMjAwNUBtYWlsLnJ1IiwiZXhwIjoxNzYyNjI4MDQyfQ.uKtyTAVNrSK6gy0czw0ichXQMfkZt-piIr3Rcwik3fw

//eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJrb3ppdHNraXkuMjAwNUBtYWlsLnJ1IiwiZXhwIjoxNzYzMjMxOTQyfQ.hxCr8R3P-YH_Xbp4S1CDk2jAT_D0KV0vsJ-cWg9l_ts
