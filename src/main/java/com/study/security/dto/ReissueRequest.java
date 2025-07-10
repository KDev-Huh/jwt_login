package com.study.security.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor      // 내부동작에서 Json 을 자바객체와 매핑할때 기본 생성자를 사용하기 때문이다.
public class ReissueRequest {
    private String refreshToken;
}
