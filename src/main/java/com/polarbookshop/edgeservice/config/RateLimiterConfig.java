package com.polarbookshop.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver keyResolver() {
        /*
        * 현재 인증된 사용자(Principal)를 가져오는 또 다른 방법은
        * 특정 HTTP 요청과 관련된 콘텍스트(리액티브 용어로 익스체인지)를 사용하는 것.
        * 이 방법을 사용하면 RateLimiter에 대한 설정을 수정할 수 있다.
        * */
        return exchange -> exchange.getPrincipal()  //현재 인증된 Principal를 현재 요청(익스체인지)에서 가져옴.
                .map(Principal::getName)    //Principal로부터 유저명을 추출
                .defaultIfEmpty("anonymous");   //요청이 인증되지 않았다면 RateLimiter를 적용하기 위한 기본 키 값으로 'anonymous' 사용
    }
}
