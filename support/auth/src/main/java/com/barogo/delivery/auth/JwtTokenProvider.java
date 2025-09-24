package com.barogo.delivery.auth;

import com.barogo.delivery.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties properties;
    private Key key;

    @PostConstruct
    void init() {
        // HS256 키
        // 설정값을 Base64/Hex/Plain 세 가지 방식으로 해석하고, 최소 32바이트(256비트) 길이를 강제합니다.
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret이 비어 있습니다. 최소 256비트 이상의 키를 설정하세요.");
        }

        byte[] keyBytes;
        String normalized = secret.trim();
        if (normalized.startsWith("base64:")) {
            keyBytes = Decoders.BASE64.decode(normalized.substring("base64:".length()));
        } else if (normalized.startsWith("hex:")) {
            keyBytes = decodeHex(normalized.substring("hex:".length()));
        } else {
            // 호환성을 위해 평문도 허용하지만, 운영에서는 base64: 접두사 사용을 권장합니다.
            keyBytes = normalized.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret의 바이트 길이가 " + (keyBytes.length * 8) + "비트로 너무 짧습니다. " +
                            "HS256 사용 시 최소 256비트(32바이트) 이상이어야 합니다. " +
                            "Base64로 인코딩한 랜덤 32바이트 이상 키를 사용하고 'base64:' 접두사를 붙여 설정하세요."
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // HEX 문자열(예: "a1b2c3...")을 바이트 배열로 디코딩합니다.
    // 공백 불가, 짝수 길이 요구, 0-9 a-f A-F 만 허용.
    private static byte[] decodeHex(String hex) {
        String s = hex.trim();
        if ((s.length() & 1) != 0) {
            throw new IllegalArgumentException("hex 시크릿은 짝수 길이여야 합니다.");
        }
        int len = s.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException("hex 시크릿에 0-9a-fA-F 이외 문자가 포함되어 있습니다.");
            }
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }


    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(properties.getAccessTokenValiditySec())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try { parse(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public String getSubject(String token) { return parse(token).getBody().getSubject(); }

    public Collection<GrantedAuthority> getAuthorities(String token) {
        Object rolesObj = parse(token).getBody().get("roles");
        if (rolesObj instanceof Collection<?> c && !c.isEmpty()) {
            // 요소를 안전하게 문자열로 변환. 엄격히 String만 허용하려면 filter(String.class::isInstance) 사용.
            return c.stream()
                    .map(String::valueOf)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public Long getMemberId(String token) {
        Object midObj = parse(token).getBody().get("mid");
        if (midObj instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("JWT에서 memberId를 찾을 수 없습니다.");
    }

}
