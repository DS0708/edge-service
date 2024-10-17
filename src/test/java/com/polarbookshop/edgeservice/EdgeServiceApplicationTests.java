package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

//일단, 레디스가 웹 세션 관련 데이터를 저장 시 Spring Context가 올바르게 로드되는지 확인

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)   //완전한 Spring Web Application Context와 Random port를 사용중인 웹 환경을 로드
@EnableAutoConfiguration(exclude = { //OAuth2/Keycloak 관련 의존성 없이도 컨텍스트가 정상적으로 로드되게 함
        ReactiveOAuth2ClientAutoConfiguration.class,
        ReactiveOAuth2ResourceServerAutoConfiguration.class})
@Testcontainers //테스트 컨테이너의 자동 시작과 종료를 활성화
class EdgeServiceApplicationTests {

    private static final int REDIS_PORT = 6379;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(REDIS_PORT));
    }

    @Test //Application Context가 올바르게 로드되었는지, Redis 연결이 성공적으로 됐는지를 확인하기 위한 테스트
    void verifyThatSpringContextLoads() {}
}

/*
* 다른 테스트 클래스에서 레디스를 사용한 세션 관리를 비활성화하려면 @TestPropertySource 에서
* 특정 테스트 클래스에 대해 spring.session.store 유형 속성을 none으로 설정하거나
* 모든 테스트 클래스에 기본 설정으로 적용하려면 속성 파일에서 설정하면 된다.
* */
