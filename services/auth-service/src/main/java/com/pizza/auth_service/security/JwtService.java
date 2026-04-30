package com.pizza.auth_service.security;

import com.pizza.auth_service.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final JwtProperties jwtProperties;
    
    private SecretKey getSignKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    //To Generate Token
    public String generateToken(UUID userId, String email){
        return Jwts.builder()
                .subject(email)
                .claim("userId",userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+jwtProperties.getExpiration()))
                .signWith(getSignKey())
                .compact();
    }

    public String extractEmail(String token){
        return extractClaim(token,Claims::getSubject);

    }

    public String extractUserId(String token){
        return extractAllClaims(token).get("userId",String.class);

    }

    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        if (extractedEmail == null || email == null) {
            return false;
        }
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }
}
