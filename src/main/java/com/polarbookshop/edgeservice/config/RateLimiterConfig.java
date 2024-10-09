package com.polarbookshop.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver keyResolver() {
        /*
        * edge-service에 보안을 추가하기 전까지는
        * 사용자 지정 KeyResolver 빈이 상수 값을 반환함으로써
        * 모든 요청이 동일한 버킷을 사용하게함
        * */
        return exchange -> Mono.just("anonymous");
    }
}
