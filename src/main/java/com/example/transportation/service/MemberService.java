package com.example.transportation.service;

import com.example.transportation.config.RedisDao;
import com.example.transportation.dto.user.MemberDto;
import com.example.transportation.entity.Authority;
import com.example.transportation.entity.Member;
import com.example.transportation.exception.CustomErrorCode;
import com.example.transportation.exception.CustomException;
import com.example.transportation.exception.ServerException;
import com.example.transportation.jwt.TokenProvider;
import com.example.transportation.repository.MemberRepository;
import com.example.transportation.utils.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisDao redisDao;

    // Validate 및 단순화 메소드

    private void LOGIN_VALIDATE(MemberDto.login request) {
        memberRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new CustomException(CustomErrorCode.LOGIN_FALSE)
                );

        if (request.getPassword().equals("google"))
            throw new CustomException(CustomErrorCode.NOT_SOCIAL_LOGIN);

        if (request.getPassword().equals("kakao"))
            throw new CustomException(CustomErrorCode.NOT_SOCIAL_LOGIN);

        if (!passwordEncoder.matches(
                request.getPassword(),
                memberRepository.findByEmail(request.getEmail())
                        .orElseThrow(
                                () -> new CustomException(CustomErrorCode.LOGIN_FALSE)
                        ).getPassword())
        ) {
            throw new CustomException(CustomErrorCode.LOGIN_FALSE);
        }
    }

    private void REGISTER_VALIDATION(MemberDto.register request) {
/*        if (request.getEmail() == null || request.getPassword() == null || request.getName() == null
                || request.getWeight() == null || request.getHeight() == null)
            throw new CustomException(REGISTER_INFO_NULL);*/
        if(request.getEmail().contains("gmail") || request.getEmail().contains("daum")){
            throw new CustomException(CustomErrorCode.WANT_SOCIAL_REGISTER);
        }

        if (memberRepository.existsByEmail(request.getEmail()))
            throw new CustomException(CustomErrorCode.DUPLICATE_USER);

        if (!request.getEmail().contains("@"))
            throw new CustomException(CustomErrorCode.NOT_EMAIL_FORM);

        if (!(request.getPassword().length() > 5))
            throw new CustomException(CustomErrorCode.PASSWORD_SIZE_ERROR);

        if (!(request.getPassword().contains("!") || request.getPassword().contains("@") || request.getPassword().contains("#")
                || request.getPassword().contains("$") || request.getPassword().contains("%") || request.getPassword().contains("^")
                || request.getPassword().contains("&") || request.getPassword().contains("*") || request.getPassword().contains("(")
                || request.getPassword().contains(")"))
        ) {
            throw new CustomException(CustomErrorCode.NOT_CONTAINS_EXCLAMATIONMARK);
        }
    }

    // Service
    // 회원가입
    @Transactional
    public ResponseEntity<MemberDto.registerResponse> register(MemberDto.register request) {
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        REGISTER_VALIDATION(request);
        memberRepository.save(
                Member.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .pnum(request.getPnum())
                        .uimg(request.getUimg())
                        .authorities(Collections.singleton(authority))
                        .build()
        );

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String atk = tokenProvider.createToken(authentication);
        String rtk = tokenProvider.createRefreshToken(request.getEmail());

        redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(14));

        return new ResponseEntity<>(MemberDto.registerResponse.response(
                request.getName(),
                request.getEmail(),
                atk,
                rtk
        ), HttpStatus.OK);
    }

    //로그인
    @Transactional
    public ResponseEntity<MemberDto.loginResponse> login(MemberDto.login request) {
        LOGIN_VALIDATE(request);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String atk = tokenProvider.createToken(authentication);
        String rtk = tokenProvider.createRefreshToken(request.getEmail());

        redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(14));

        return new ResponseEntity<>(MemberDto.loginResponse.response(
                atk,
                rtk
        ), HttpStatus.OK);
    }

    // accessToken 재발급
    @Transactional
    public ResponseEntity<MemberDto.loginResponse> reissue(String rtk) {
        String username = tokenProvider.getRefreshTokenInfo(rtk);
        String rtkInRedis = redisDao.getValues(username);

        if (Objects.isNull(rtkInRedis) || !rtkInRedis.equals(rtk))
            throw new ServerException(CustomErrorCode.REFRESH_TOKEN_IS_BAD_REQUEST); // 410

        return new ResponseEntity<>(MemberDto.loginResponse.response(
                tokenProvider.reCreateToken(username),
                null
        ), HttpStatus.OK);
    }

    // 로그아웃
    public ResponseEntity<Status> logout(String auth) {
        String atk = auth.substring(7);
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        if (redisDao.getValues(email) != null) {
            redisDao.deleteValues(email);
        }

        redisDao.setValues(atk, "logout", Duration.ofMillis(
                tokenProvider.getExpiration(atk)
        ));
        return new ResponseEntity<>(Status.LOGOUT_TRUE, HttpStatus.OK);
    }

}
