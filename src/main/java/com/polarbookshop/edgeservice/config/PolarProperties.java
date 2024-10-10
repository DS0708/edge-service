package com.polarbookshop.edgeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "polar")  //클래스의 필드에 외부 설정 파일(.properties, .yml)의 값들을 바인딩하는 데 사용(@ConfigurationPropertiesScan를 같이 작성해줘야함)
public class PolarProperties {
    private String greeting; //사용자 정의 속성인 polar.greeting( prefix + 필드명 ) 속성이 문자열로 인식되는 필드

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
