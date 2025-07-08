package com.study.security.service;

import com.study.security.domain.User;
import com.study.security.dto.LoginRequest;
import com.study.security.dto.SignupRequest;
import com.study.security.jwt.JwtProvider;
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

    public void signup(SignupRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자입니다.");
        }
        String encodePassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodePassword, "ROLE_USER");

        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        return jwtProvider.generateToken(user.getUsername(), user.getRole());
    }
}
