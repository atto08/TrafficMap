package com.example.transportation.config;

import com.example.transportation.jwt.JwtAuthFilter;
import com.example.transportation.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity //스프링 Security지원을 가능하게 함.
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnDefaultWebSecurity
@RequiredArgsConstructor
public class WebMvcConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>
        implements WebMvcConfigurer, AuthenticationEntryPoint {
    private final TokenProvider tokenProvider;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .exposedHeaders("Authorization", "RefreshToken")
                .allowCredentials(true);
    }

    //권한 없이 접근 시도시 401 에러 내보냄.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity
//                .authorizeHttpRequests((authz) -> authz
//                        // 어떤 요청이든 '인증'
//                        .anyRequest().authenticated())

        httpSecurity.cors().and().csrf().disable()

                //h2-console을 위한 설정 추가
                .headers()
                .frameOptions()
                .sameOrigin()

                //security는 기본적으로 세션을 사용하나 본 프로젝트에서는 세션을 사용하지 않으므로 세션 설정을 Stateless로 변경.
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest()
                .permitAll()

                .and()
                .apply(new WebMvcConfig(tokenProvider));

        return httpSecurity.build();
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        JwtAuthFilter customFilter = new JwtAuthFilter(tokenProvider);
        builder.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
