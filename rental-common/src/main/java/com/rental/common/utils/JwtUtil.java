package com.rental.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:rental-system-secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    public String generateToken(Long userId, String phone, Integer userType) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration * 1000);
        
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("phone", phone)
                .withClaim("userType", userType)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }
    
    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new JWTVerificationException("Token验证失败");
        }
    }
    
    public Long getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asLong();
    }
    
    public String getPhoneFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("phone").asString();
    }
    
    public Integer getUserTypeFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userType").asInt();
    }
    
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }
}