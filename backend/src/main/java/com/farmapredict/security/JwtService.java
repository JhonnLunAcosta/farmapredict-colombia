package com.farmapredict.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generate(String username, String rol) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String username(String token) {
        return claim(token, Claims::getSubject);
    }

    public boolean valid(String token, UserDetails user) {
        return username(token).equals(user.getUsername()) && !expired(token);
    }

    private boolean expired(String token) {
        return claim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T claim(String token, Function<Claims, T> fn) {
        return fn.apply(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload());
    }
}
