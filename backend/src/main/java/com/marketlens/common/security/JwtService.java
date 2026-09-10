package com.marketlens.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.marketlens.common.error.UnauthorizedException;
import com.marketlens.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String TOKEN_TYPE = "typ";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";
    private static final String ROLES = "roles";
    private static final String NAME = "name";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .claim(TOKEN_TYPE, ACCESS)
                .claim(NAME, user.getDisplayName())
                .claim("username", user.getUsername())
                .claim(ROLES, user.getRoles().stream().map(Enum::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTokenTtl())))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .claim(TOKEN_TYPE, REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getRefreshTokenTtl())))
                .signWith(key)
                .compact();
    }

    public AccessToken parseAccessToken(String token) {
        Claims claims = parse(token);
        if (!ACCESS.equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new UnauthorizedException("Not an access token");
        }
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(ROLES, List.class);
        return new AccessToken(
                UUID.fromString(claims.getSubject()),
                claims.get("username", String.class),
                claims.get(NAME, String.class),
                roles == null ? List.of() : roles);
    }

    public UUID parseRefreshSubject(String token) {
        Claims claims = parse(token);
        if (!REFRESH.equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new UnauthorizedException("Not a refresh token");
        }
        return UUID.fromString(claims.getSubject());
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    public record AccessToken(UUID userId, String username, String displayName, List<String> roles) {
    }
}
