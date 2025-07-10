package com.study.security.service;

import com.study.security.domain.RefreshToken;
import com.study.security.domain.User;
import com.study.security.dto.LoginRequest;
import com.study.security.dto.ReissueRequest;
import com.study.security.dto.SignupRequest;
import com.study.security.dto.TokenResponse;
import com.study.security.jwt.JwtProvider;
import com.study.security.repository.RefreshTokenRedisRepository;
import com.study.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void signup(SignupRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자입니다.");
        }
        String encodePassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodePassword, "ROLE_USER");

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 발급
        String accessToken = jwtProvider.generateToken(user.getUsername(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUsername());

        // Redis 에 저장
        RefreshToken redisToken = new RefreshToken(user.getUsername(), refreshToken);
        refreshTokenRedisRepository.save(redisToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.getRefreshToken();

        if(!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.");
        }

        String username = jwtProvider.getUsernameFromToken(refreshToken);

        RefreshToken saved = refreshTokenRedisRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("저장된 토큰이 없습니다."));

        if(!saved.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        String role = user.getRole();
        String newAccessToken = jwtProvider.generateToken(username, role);

        return new TokenResponse(newAccessToken, refreshToken);
    }
}
