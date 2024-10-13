package com.polarbookshop.edgeservice.config;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@WebFluxTest
@Import(SecurityConfig.class)
class SecurityConfigTests {

    @Autowired
    WebTestClient webClient;

    //클라이언트 등록에 대한 정보를 가져올 때 키클록과의 상호작용을 실제로 하지 않기 위한 모의 빈
    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenLogoutNotAuthenticatedAndNoCsrfTokenThen403() {
        webClient
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void whenLogoutAuthenticatedAndNoCsrfTokenThen403() {
        webClient
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
                .post()
                .uri("/logout")
                .exchange()
                .expectStatus().isForbidden();
    }

    /*
     * 애플리케이션이 올바른 OIDC 로그인 및 CSRF 콘텍스트로
     * /logout에 HTTP POST 요청을 보내면 HTTP 302 응답을 받는지 확인
     * */
    @Test
    void whenLogoutAuthenticatedAndWithCsrfTokenThen302() {
        when(clientRegistrationRepository.findByRegistrationId("test"))
                .thenReturn(Mono.just(testClientRegistration()));

        webClient
                //사용자 인증을 위한 모의 ID 토큰 사용
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin())
                //요청에 CSRF 토큰 추가
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/logout")
                .exchange()
                //로그아웃을 키클록으로 전파하기 위한 302 리다이렉션이 응답이어야 한다.
                .expectStatus().isFound();
    }

    private ClientRegistration testClientRegistration() {
        //키클록에 연결할 URL을 얻기 위해 Spring Security가 사용하는 ClientRegistration 모의 객체
        return ClientRegistration.withRegistrationId("test")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("test")
                .authorizationUri("https://sso.polarbookshop.com/auth")
                .tokenUri("https://sso.polarbookshop.com/token")
                .redirectUri("https://polarbookshop.com")
                .build();
    }

}
