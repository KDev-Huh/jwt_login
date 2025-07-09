package com.study.security.config;

import com.study.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception { // 매개변수들 스프링이 자동으로 넣어줌
        http        // Spring Security 보안 설정 객체
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (REST API는 보통 꺼둠)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // 로그인/회원가입 허용
                        .anyRequest().authenticated() // 그 외는 인증 필요
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);    // UsernamePasswordAuthenticationFilter 이 필터 앞에 필터 삽입

        return http.build();       // http 보안 설정을 마무리하고 SecurityFilterChain 으로 변환
    }
}
// 스프링이 @Configuration 이 붙은 클래스를 보고 그 안에 @Bean 이 붙은 모든 메서드를 실행하고 스프링 컨테이너에 등록한다.
// 이때 SecurityFilterChain 을 반환하는 메서드가 있다면 그 객체를 기본 SecurityConfig 설정과 바꿔준다.