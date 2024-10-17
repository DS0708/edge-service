package com.polarbookshop.edgeservice.config;

/*
* 보안과 관련된 모든 설정을 여기서 관리
* */

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity //Spring Security WebFlux 지원 활성화
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        /*
        * Spring에 의해 Autowire되는 ServerHttpSecurity 객체는 Spring Security 설정과 SecurityWebFilterChain 빈 생성을 위한 편리한 DSL을 제공한다.
        * authorizeExchange()를 사용하면 모든 요청에 대한 액세스 정책을 정의할 수 있다. (리액티브 스프링에서는 요청을 'Exchange'라고 부른다.)
        * */
        return http
                //모든 요청에 대한 인증이 이뤄져야 한다.
                .authorizeExchange(exchange -> exchange.anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}
