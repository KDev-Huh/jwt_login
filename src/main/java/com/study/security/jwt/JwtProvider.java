package com.study.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")     // application.yml에 있는 값 들고오기
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidity;

    private Key key;

    @PostConstruct  // 서버시작후 한번 실행되는 메서드
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()                // 서명에 사용하는 키를 넣어서 파서를 만들고
                    .parseClaimsJws(token); // token 을 진짜 파싱
            return true;
        } catch (Exception e) {     // payload 나 header 가 수정되거나 signature 가 잘못되었다면 예외가 발생한다.
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String role) {     // 문자열 형태인 권한을 SimpleGrantedAuthority 로 감싸준다.
        return List.of(new SimpleGrantedAuthority(role));       // Spring Security 는 내부적으로 사용자 권한을 문자열이 아닌 GrantedAuthority
    }                                                         // 형태로 관리하기 때문에 SimpleGrantedAuthority 로 감싸준다.
}
