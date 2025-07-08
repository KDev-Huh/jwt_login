package com.study.security.service;

import com.study.security.domain.User;
import com.study.security.dto.SignupRequest;
import com.study.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자입니다.");
        }
        String encodePassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodePassword, "ROLE_USER");

        userRepository.save(user);
    }
}
