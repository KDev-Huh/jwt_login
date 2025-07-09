package com.study.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {     // OncePerRequestFilter 가 이 필터를 한번만 실행하도록 설정해준다.
    private final JwtProvider jwtProvider;

    @Override       // OncePerRequestFilter 안에 doFilter() 함수에서 doFilterInternal 함수를 실행시켜준다.
    protected void doFilterInternal(                    // 아래 매개변수들은 Servlet 컨테이너가 HTTP 요청을 처리하면서,
            HttpServletRequest request,                 // Spring Security 가 등록한 필터 체인을 실행하고 이 필터 체인의 한 부분인
            HttpServletResponse response,               // JwtAuthenticationFilter#doFilterInternal()을 호출할 때 → 자동으로 이 3개를 인자로 넣어준다.
            FilterChain filterChain             // 다음 필터로 넘겨주기 위해 받는다.
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if(token != null && jwtProvider.validateToken(token)) {
            String username = jwtProvider.getUsernameFromToken(token);
            String role = jwtProvider.getRoleFromToken(token);

            // 스프링 인증 객체 생성(이 사용자가 인증된 사용자다 라는 정보를 Spring Security 가 알 수 있도록 만드는 인증 객체)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, jwtProvider.getAuthorities(role));
                    // 이미 jwt 를 통해 로그인 인증을 했기 때문에 password 에는 null 을 넣어준다.

            authentication.setDetails(  // 요청의 부가정보(IP 주소, 세션 ID 등)를 설정한다.
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 인증 정보 등록
            // SecurityContextHolder => 현재 스레드의 "보안 컨텍스트"를 저장하는 객체
            // .setAuthentication(authentication); 현재 요청은 이 인증된 사용자에 의해 수행되었다고 시스템에 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Spring Security 에게 "지금 이 요청은 인증된 사용자야"라고 공식 선언하는 코드이다.
        }

        filterChain.doFilter(request, response);    // 다음 필터로 넘기기
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if(bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);     // 인덱스 7부터 끝까지 잘라서 가져옴
        }
        return null;
    }
}
