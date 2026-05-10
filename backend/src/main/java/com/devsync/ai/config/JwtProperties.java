package com.devsync.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "devsync.jwt")
public class JwtProperties {

    /** HMAC SHA key — UTF-8 string; must be at least 256 bits (32 bytes). */
    private String secret;

    /** Access token TTL in seconds */
    private long expirationSeconds = 86400;

    private String issuer = "devsync-ai";

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalStateException(
                    "devsync.jwt.secret must be at least 32 bytes (UTF-8); set DEVSYNC_JWT_SECRET in production.");
        }
    }
}
