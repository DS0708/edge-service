package com.polarbookshop.edgeservice.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

// 함수적 방법을 사용해 FallBack EndPoint를 선언
@Configuration
public class WebEndpoints {

    @Bean //함수형 REST 엔드포인트가 빈 내부에서 정의
    public RouterFunction<ServerResponse> routerFunction() {
        /*
        * Spring WebFlux에서 함수형 엔드포인트는 RouterFunction<ServerResponse> 빈에서
        * RouterFunctions가 제공하는 Fluent API를 통해 라우트로 정의한다.
        * 각 라우트에 대해 엔드포인트 URL, 메서드, 핸드러를 정의해야 한다.
        * */
        return RouterFunctions.route() //라우트를 생성하기 위한 Fluent API 제공
                //GET 엔드포인트에 대한 폴백 응답, 편의상 빈 문자열 반환
                .GET("/catalog-fallback", request -> ServerResponse.ok().body(Mono.just(""), String.class))
                //POST 엔드포인트에 대한 폴백 응답, 503 오류 반환 -> 실제 시나리오에서는 클라이언트가 처리할 수 있도록 사용자 지정 예외 발생 또는 원래 요청에 대해 캐시에 저장된 마지막 값 반환 전략을 채택할 수 있음.
                .POST("/catalog-fallback", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build())
                .build(); //함수형 엔드포인트를 만든다.
    }
}
