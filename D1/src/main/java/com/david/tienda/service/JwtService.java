package com.david.tienda.service;

import com.david.tienda.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Crea y valida los JWT. El secreto y la expiración vienen de application.yaml (variables de entorno). */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        SecretKey secretKey;
        try {
            // El secreto se genera con "openssl rand -base64 32" (256 bits, el mínimo para HS256)
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "JWT_SECRET debe ser un texto Base64 de al menos 32 bytes (openssl rand -base64 32)", e);
        }
        this.key = secretKey;
        this.expirationMillis = expirationMinutes * 60_000L;
    }

    /** subject = username; claims userId y rolId; firmado con HS256. */
    public String generate(Users user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("rolId", user.getRol().getId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    /** Verifica firma y expiración. Lanza JwtException (o una subclase) si el token no sirve. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Integer getUserId(Claims claims) {
        return numericClaim(claims, "userId");
    }

    public Integer getRolId(Claims claims) {
        return numericClaim(claims, "rolId");
    }

    private Integer numericClaim(Claims claims, String name) {
        Object value = claims.get(name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new JwtException("El token no trae el claim " + name);
    }
}
