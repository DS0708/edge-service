package com.polarbookshop.edgeservice.config;

/*
* 보안과 관련된 모든 설정을 여기서 관리
* */

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@EnableWebFluxSecurity //Spring Security WebFlux 지원 활성화
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository
            ) {
        /*
        * Spring에 의해 Autowire되는 ServerHttpSecurity 객체는 Spring Security 설정과 SecurityWebFilterChain 빈 생성을 위한 편리한 DSL을 제공한다.
        * authorizeExchange()를 사용하면 모든 요청에 대한 액세스 정책을 정의할 수 있다. (리액티브 스프링에서는 요청을 'Exchange'라고 부른다.)
        * */
        return http
                //모든 요청에 대한 인증이 이뤄져야 한다.
                .authorizeExchange(exchange -> exchange.anyExchange().authenticated())
                //ServerHttpSecurity는 Spring Security에서 OAuth2 클라이언트 설정을 위해 oauth2Login()와 oauth2Client()라는 2가지 방법을 제공.
                //oauth2Login()를 사용하면 애플리케이션이 OAuth2 클라이언트 역할을 할 수 있도록 설정할 수 있으며, OIDC(Open ID Connect)를 통해 사용자 인증 가능
                //oauth2Client()를 사용하면 애플리케이션은 사용자를 인증하지 않고 대신 인증 메커니즘 정의를 사용자에게 맡긴다.
                //지금은 OIDC 인증을 원하기 때문에 oauth2Login() 사용
                .oauth2Login(Customizer.withDefaults())
                .logout(logout-> logout.logoutSuccessHandler( //로그아웃이 성공적으로 완료되는 경우에 대한 사용자 지정 핸들러 정의
                        oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .build();
    }
    /*
    * Spring Security는 '/logout' 엔드포인트에 POST 요청을 보내 로그아웃을 할 수 있도록 지원하고,
    * 로그아웃 시 사용자와 관련된 모든 세션 데이터를 삭제한다.
    * 하지만 키클록에는 여전히 이 사용자에 대한 세션을 가지고 있으므로,
    * 사용자를 완전히 로그아웃 하려면 키클록에도 로그아웃 요청이 전달되어야 한다.
    * 이를 위해 Spring Security는 'OIDC RP-주도 로그아웃'사양을 구현하는데, 이 사양은 신뢰 당사자(RP)인 OAuth2 클라이언트에서
    * 인증 서버로 로그아웃 요청을 전파하는 방법을 정의한다.
    * Spring Security는 이 시나리오를 완벽하게 지원하며 로그아웃 요청을 키클록으로 전파하는 방법을 설정하기 위해 사용할 수 있는
    * OidcClientInitiatedServerLogoutSuccessHandler 객체를 제공한다.
    */

    /*
    * RP 주도 로그아웃 기능이 활성화되어 있다고 가정한다면,
    * 사용자 로그아웃을 Spring Security가 처리한 후 edge-service는
    * 브라우저를 통해(리다이렉션 사용) 키클록에 로그아웃 요청을 보낸다.
    * 이 요청을 받고 인증 서버(키클록)는 로그아웃 작업을 수행한 다음 사용자를 다시 애플리케이션으로 리다이렉션할 수 있다.
    * 이때, OidcClientInitiatedServerLogoutSuccessHandler 클래스의 setPostLogoutRedirectUri() 메서드를 사용하면
    * 로그아웃 후 사용자를 어디로 리다이렉션해야 하는지 설정할 수 있다.
    * 직접 URL을 지정해도 되지만 클라우드 환경에서는 호스트 이름, 서비스 이름, 프로토콜(http 대 https)과 같은 변수로 인해 잘 작동하지 않을 수도 있으며,
    * 이를 위해 Spring Security는 런타임에 동적으로 URL을 결정할 수 있도록 {baseUrl} 플레이스홀더에 대한 지원을 한다.
    * 만약 edge-service를 로컬에서 실행하면 이 플레이홀더는 http://localhost:9000으로 바뀌며,
    * 클라우드에서 TLS 종료와 함께 프록시 뒤에서 에지 서비스를 실행하고 DNS 이름 polarbookshop.com 으로 액세스한다면,
    * 자동으로 https://polarbookshop.com으로 대체된다.
    * 그러나 키클록에서 클라이언트를 설정할 때는 정확한 URL이 필요하며 프로덕션 환경에서는
    * 유효한 리다이렉션 목록에 http://localhost:9000를
    * 실제 프로덕션 환경과 일치하는 올바른 URL로 변경을 해줘야 한다.
    * */
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository
            /*
            * ReactiveClientRegistrationRepository 빈은 키클록에 등록된 클라이언트에 대한 정보를 저장하기위해
            * 스프링부트에 의해 자동으로 설정되며 Spring Security에서 인증과 권한을 위해 사용한다. 여기서는 클라이언트만 있는데
            * 앞서 application.yaml에서 설정한 클라이언트이다.
            * */
    ) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        //OIDC 공급자인 키클록에서 로그아웃 후 사용자를 스프링에서 동적으로 지정하는 애플리케이션 베이스 URL로 리다이렉션한다.(로컬에서는 http://localhost:9000)
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }
}
