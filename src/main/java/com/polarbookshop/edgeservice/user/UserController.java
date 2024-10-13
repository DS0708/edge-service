package com.polarbookshop.edgeservice.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/* 1번 방법
* 채택된 인증 전략(사용자 이름/암호, OIDC/OAuth2, SAML2)이 무엇이든 Spring Security는
* 인증된 사용자(Principal)에 대한 정보를 Authentication을 구현하는 객체의 한 필드에 갖는다.
* OIDC의 경우 Principal 객체는 OicdUser 인터페이스 유형이며 Spring Security는 여기에 ID 토큰을 저장한다.
* 그리고 Authentication은 SecurityContext 객체의 한 필드에 저장된다.
* 따라서 SecurityContext에 접근하면, 현재 로그인한 사용자의 인증 객체 정보를 불러올 수 있고,
* SecurityContext에 접근하는 방법은 ReactiveSecurityContextHolder(Imperative Application의 경우는 SecurityContextHolder)에서 검색하는 것이다.
* ---
* ReactiveSecurityContextHolder (접근)-> SecurityContext (한 필드로 소유)-> Authentication (한 필드로 소유)-> OidcUser(Principal) (한 필드로 소유)-> OidcIdToken(ID 토큰)
* ---
* */

/* 2번 방법
* ReactiveSecurityContextHolder를 직접 사용하는 방법 외에도, Spring WebMVC 와 WebFlux에서는
* @CurrentSecurityContext 및 @AuthenticationPrincipal 어노테이션을 통해 SecurityContext와 Principal(이 경우 OidcUser)를 주입할 수 있다.
* */

@RestController
public class UserController {

    //1번 방법
//    @GetMapping("user")
//    public Mono<User> getUser() {
//        return ReactiveSecurityContextHolder.getContext() //ReactiveSecurityContextHolder로부터 현재 인증된 사용자에 대한 SecurityContext를 가져옴.
//                .map(SecurityContext::getAuthentication) //SecurityContext로부터 Authentication를 받음.
//                .map(authentication -> (OidcUser)authentication.getPrincipal()) //Authentication에서 프린시플을 가져옴. OIDC의 경우 프린시플은 OidcUser 유형.
//                .map(oidcUser -> //OidcUser의 ID 토큰에서 추출한 데이터를 사용해 User 객체 생성
//                        new User(
//                                oidcUser.getPreferredUsername(),
//                                oidcUser.getGivenName(),
//                                oidcUser.getFamilyName(),
//                                List.of("employee","customer")
//                        ));
//    }

    //2번 방법
    @GetMapping("user")
    public Mono<User> getUser(
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        var user = new User(
                        oidcUser.getPreferredUsername(),
                        oidcUser.getGivenName(),
                        oidcUser.getFamilyName(),
                        List.of("employee","customer")
                );
        return Mono.just(user); //edge-service는 Reactive Application이기 때문에 Reactive Stream으로 감싼다.
    }
    /*
    * /user 엔드포인트에 액세스할 때 키클록으로 리다이렉션되면, 키클록은 사용자의 유저명과 패스워드를 확인한다.
    * 성공적으로 확인하면, 키클록은 edge-service를 다시 호출하고 새로 인증된 사용자의 ID 토큰을 보낸다.
    * 그러면, edge-service는 토큰을 저장하고 세션 쿠키와 함께 필요한 엔드포인트 브라우저를 리다이렉션한다.
    * 이 시점부터 브라우저와 edge-service 간의 모든 통신에서는 해당 세션 쿠키를 통해 사용자의 인증 콘텍스트를 식별한다.
    * */
}
