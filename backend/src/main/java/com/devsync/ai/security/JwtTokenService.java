package com.devsync.ai.security;

import com.devsync.ai.config.JwtProperties;
import com.devsync.ai.model.Role;
import com.devsync.ai.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(User user) {
        InstantRange range = expiryRange();
        String subject = user.getId().toString();

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(subject)
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(Role::getName).toList())
                .issuedAt(range.start())
                .expiration(range.exp())
                .signWith(signingKey())
                .compact();
    }

    private InstantRange expiryRange() {
        long millis = jwtProperties.getExpirationSeconds() * 1000;
        Date start = new Date();
        Date exp = new Date(start.getTime() + millis);
        return new InstantRange(start, exp);
    }

    public Claims parseAndValidateClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private record InstantRange(Date start, Date exp) {}
}
