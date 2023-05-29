package com.example.transportation.service;


import com.example.transportation.config.RedisDao;
import com.example.transportation.dto.oauth.GoogleOAuthTokenDto;
import com.example.transportation.dto.oauth.KakaoOAuthTokenDto;
import com.example.transportation.dto.user.GoogleUserInfoDto;
import com.example.transportation.dto.user.KakaoUserInfoDto;
import com.example.transportation.dto.user.MemberDto;
import com.example.transportation.entity.Authority;
import com.example.transportation.entity.Member;
import com.example.transportation.jwt.TokenProvider;
import com.example.transportation.oauth.GoogleOAuth;
import com.example.transportation.oauth.KakaoOAuth;
import com.example.transportation.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final KakaoOAuth kakaoOAuth;
    private final GoogleOAuth googleOAuth;
    private final TokenProvider tokenProvider;
    private final RedisDao redisDao;
    // validate 및 단순 메소드
    Authority authority = Authority.builder()
            .authorityName("ROLE_USER")
            .build();

    private ResponseEntity<MemberDto.socialLoginResponse> Login(Member memberInfo) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(memberInfo.getEmail(), memberInfo.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String atk = tokenProvider.createToken(authentication);
        String rtk = tokenProvider.createRefreshToken(memberInfo.getEmail());

        redisDao.setValues(memberInfo.getEmail(), rtk, Duration.ofDays(14));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + atk);
        httpHeaders.add("Refresh-token",rtk);

        MemberDto.socialLoginResponse httpBody = MemberDto.socialLoginResponse.response(
                memberInfo.getName(), memberInfo.getEmail(), memberInfo.getProfileImageUrl(), "SOCIAL_LOGIN_TRUE");

        return new ResponseEntity<>(httpBody, httpHeaders, HttpStatus.OK);
    }


    // 구글 서버에 파라미터를 전송 후 accessToken 을 획득 후 전송하여 유저의 정보를 가져오는 과정
    private GoogleUserInfoDto getGoogleUserInfoDto(String code) throws JsonProcessingException {

        ResponseEntity<String> accessTokenResponse = googleOAuth.requestAccessToken(code);
        GoogleOAuthTokenDto oAuthToken = googleOAuth.getAccessToken(accessTokenResponse);

        ResponseEntity<String> userInfoResponse = googleOAuth.requestUserInfo(oAuthToken);
        GoogleUserInfoDto googleUser = googleOAuth.getUserInfo(userInfoResponse);
        return googleUser;
    }

    // Service
    // 구글 로그인 서비스
    @Transactional
    public ResponseEntity<MemberDto.socialLoginResponse> googleLogin(String code) throws IOException {
        GoogleUserInfoDto googleUser = getGoogleUserInfoDto(code);

        String email = googleUser.getEmail();
        String name = googleUser.getName();
        String profileImage = googleUser.getPicture();
        String password = passwordEncoder.encode("google");

        Member member = new Member(name, email, password, profileImage);

        // 첫 로그인시 사용자 정보를 보내줌
        if (!memberRepository.existsByEmail(email)) {
            memberRepository.save(member);
            return new ResponseEntity<>(MemberDto.socialLoginResponse.response(
                    name, email, profileImage, "SOCIAL_REGISTER_TRUE"), HttpStatus.OK);
        }
        // 이메일이 존재할시 로그인
        return Login(member);
    }


//        private KakaoUserInfoDto getKakaoUserInfoDto(String code) throws JsonProcessingException {
//        ResponseEntity<String> accessTokenResponse = kakaoOAuth.requestAccessToken(code);
//        KakaoOAuthTokenDto oAuthToken = kakaoOAuth.getAccessToken(accessTokenResponse);
//        ResponseEntity<String> userInfoResponse = kakaoOAuth.requestUserInfo(oAuthToken);
//        KakaoUserInfoDto kakaoUser = kakaoOAuth.getUserInfo(userInfoResponse);
//        return kakaoUser;
//    }
    // 카카오 로그인 서비스
//    @Transactional
//    public ResponseEntity<MemberDto.socialLoginResponse> kakaoLogin(String code) throws IOException {
//        KakaoUserInfoDto kakaoUser = getKakaoUserInfoDto(code);
//        String email = kakaoUser.getKakao_account().getEmail();
//        String name = kakaoUser.getProperties().getNickname();
//        String profileImagePath = kakaoUser.getProperties().getProfile_image();
//
//        // 첫 로그인시 사용자 정보를 보내줌
//        if (!memberRepository.existsByEmail(email)) {
//            return new ResponseEntity<>(MemberDto.socialLoginResponse.response(
//                    name, email, profileImagePath, null, null, "SOCIAL_REGISTER_TRUE"
//            ), HttpStatus.OK);
//        }
//        // 이메일이 존재할시 로그인
//        return Login(email, name, profileImagePath);
//    }

    // 추가 정보 요청 서비스
    @Transactional
    public ResponseEntity<MemberDto.registerResponse> socialRegister(MemberDto.register request) {
        String social = null;

        if (request.getEmail().contains("gmail")) {
            social = "google";
        }

        memberRepository.save(
                Member.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(social))
                        .profileImageUrl(request.getProfileImageUrl())
                        .authorities(Collections.singleton(authority))
                        .build()
        );

        Optional<Member> memberPassword = memberRepository.findByEmail(request.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), memberPassword);
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
}
