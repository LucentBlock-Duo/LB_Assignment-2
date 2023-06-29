package com.lucentblock.assignment2.security.authentication.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtRefreshService { // extend JwtService 로 리팩토링 고려
    @Value("${application.security.jwt.refresh.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh.secret-key}")
    private String secretKey;

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            return true;
        }

        return false;
    }

    public String extractRole(String token) {
        final Claims claims = extractAllClaims(token);

        if (claims != null) {
            return (String) claims.get("role");
        }

        return null;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);

        if (claims != null) {
            return claimsResolver.apply(claims);
        }

        return null;
    }


    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("This Refresh Token is Expired");
        } catch (JwtException e) {
            log.info("This Refresh Token is Invalid");
        }

        return null;
    }
//    private Date extractExpiration(String token) { // 이 메소드 삭제여부 검토
//        return extractClaim(token, Claims::getExpiration);

//    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 1일의 유효기간
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}