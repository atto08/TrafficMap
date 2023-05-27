package com.example.transportation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;


import java.util.Optional;

//@EnableJpaAuditing // jpa Auditing 기능 활성화
@Configuration
public class JpaConfig {
    @Bean
    public AuditorAware<String> auditorAware(){
        return () -> Optional.of("uno"); // 스프링 시큐리티로 인증 기능을 붙이게 될 때, 수정
    }
}
