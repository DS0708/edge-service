package com.polarbookshop.edgeservice.user;

import java.util.List;

import com.polarbookshop.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTests {
    @Autowired
    WebTestClient webClient;

    //클라이언트 등록에 대한 정보를 가져올 때 키클록과의 상호작용을 실제로 하지 않기 위한 모의 빈
    @MockBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    /*
    *  요청이 인증되지 않은 경우 HTTP 401 응답을 반환하도록 에지 서비스를 설정했기 때문에
    *  먼저 인증하지않고 "/user" 엔드포인트를 호출할 때 그 응답을 받는지 확인해야한다.
    * */
    @Test
    void whenNotAuthenticatedThen401(){
        webClient
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /*
    * 사용자가 인증되는 시나리오를 테스트하기 위해 SecurityMockServerConfigurers가 제공하는
    * 설정 객체인 mockOidcLogin()을 사용해 OIDC 로그인을 모의로 실행하고 ID 토큰을 만든 후,
    * WebTestClient에서 요청 콘텍스트를 이에 맞춰 변경할 수 있다.
    *
    * "/user" 엔드포인트는 OidcUser 객체를 통해 ID 토큰의 클레임을 읽기 때문에 유저명, 이름, 성으로
    * ID 토큰을 생성해야한다.(역할은 현재 컨트롤러에 하드코딩 되어 있음)
    * */
    @Test
    void whenAuthenticatedThenReturnUser() {
        //예상되는 인증 사용자
        var expectedUser = new User("jon.snow", "Jon", "Snow", List.of("employee", "customer"));

        webClient
                //OIDC에 기반해 인증 콘텍스트를 정의하고 예상되는 사용자를 사용
                .mutateWith(configureMockOidcLogin(expectedUser))
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                //현재 인증된 사용자와 동일한 정보를 갖는 User 객체 예상
                .expectBody(User.class)
                .value(user -> assertThat(user).isEqualTo(expectedUser));
    }

    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidcLogin(User expectedUser) {
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(builder -> { //모의 ID 토큰을 생성
            builder.claim(StandardClaimNames.PREFERRED_USERNAME, expectedUser.username());
            builder.claim(StandardClaimNames.GIVEN_NAME, expectedUser.firstName());
            builder.claim(StandardClaimNames.FAMILY_NAME, expectedUser.lastName());
        });
    }
}
