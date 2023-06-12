package com.example.transportation.service;

import com.example.transportation.dto.response.GoogleMemberInfoDto;
import com.example.transportation.entity.Member;
import com.example.transportation.entity.MemberRoleEnum;
import com.example.transportation.jwt.JwtAuthFilter;
import com.example.transportation.jwt.TokenDto;
import com.example.transportation.jwt.TokenProvider;
import com.example.transportation.repository.MemberRepository;
import com.example.transportation.security.MemberDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final String GOOGLE_TOKEN_REQUEST_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USERINFO_REQUEST_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @Value("${ADMIN_TOKEN}")
    private String ADMIN_TOKEN;
    @Value("${app.google.redirectUri}")
    private String redirectUri;
    @Value("${app.google.clientId}")
    private String googleClientId;
    @Value("${app.google.clientSecret}")
    private String googleClientSecret;



    //구글 로그인
    public ResponseEntity<?> googleLogin(String code) throws JsonProcessingException {
        //"인가 코드"로 "엑세스 토큰" 요청
        String accessToken = getAccessToken(code);

        //"액세스 토큰"으로 "구글 사용자 정보" 가져오기
        GoogleMemberInfoDto googleMemberInfoDto = getGoogleMemberInfo(accessToken);

        //"구글 사용자 정보"로 필요시 회원가입
        Member googleMember = registerGoogleUserIfNeeded(googleMemberInfoDto);

        MemberDetailsImpl memberDetails = new MemberDetailsImpl(googleMember);

        UsernamePasswordAuthenticationToken toAuthentication = new UsernamePasswordAuthenticationToken(memberDetails.getSubId(), memberDetails.getPassword());

        TokenDto tokenDto = tokenProvider.generateTokenDto(toAuthentication);

        HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.add(JwtAuthFilter.AUTHORIZATION_HEADER , JwtAuthFilter.BEARER_PREFIX + tokenDto.getAccessToken());
        httpHeaders.add("Refresh-Token" , tokenDto.getRefreshToken());

        //강제 로그인 처리
        forceLogin(googleMember);

        return new ResponseEntity<>("로그인 완료", httpHeaders, HttpStatus.OK);
    }

    //액세스 토큰 요청
    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                GOOGLE_TOKEN_REQUEST_URL,
                HttpMethod.POST,
                googleTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("access_token").asText();

        return accessToken;
    }

    //토큰으로 구글 API 호출
    private GoogleMemberInfoDto getGoogleMemberInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                GOOGLE_USERINFO_REQUEST_URL,
                HttpMethod.GET,
                googleUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String id = jsonNode.get("id").asText();
        String email = jsonNode.get("email").asText();
        String name = jsonNode.get("name").asText();
        String profileImgUrl = jsonNode.get("picture").asText();

        return new GoogleMemberInfoDto(id, email ,name, profileImgUrl);
    }


    private Member registerGoogleUserIfNeeded(GoogleMemberInfoDto googleMemberInfoDto) {
        // DB 에 중복된 Google Id 가 있는지 확인
        String email = googleMemberInfoDto.getEmail();
        String googleId = googleMemberInfoDto.getId();
        Member googleUser = memberRepository.findByEmailAndGoogleId(email, googleId).orElse(null);
        if (googleUser == null) {
            // 회원가입
            // username: google nickname
            String name = googleMemberInfoDto.getName();

            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            String profileImgUrl = googleMemberInfoDto.getProfileImgUrl();
            String subId = googleMemberInfoDto.getId();

            googleUser = new Member (email, subId, name, encodedPassword, profileImgUrl, MemberRoleEnum.USER);

            memberRepository.save(googleUser);
        }
        return googleUser;
    }


    private void forceLogin(Member googleMember) {
        MemberDetailsImpl memberDetails = new MemberDetailsImpl(googleMember);
        Authentication authentication = new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    public ResponseEntity<?> getAdmin(String email, String googleId, String adminToken) {
        Optional<Member> member = memberRepository.findByEmailAndGoogleId(email, googleId);

        if (member.isEmpty()) {
            return new ResponseEntity<>("해당 유저가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if (Objects.equals(ADMIN_TOKEN, adminToken)) {
            member.get().updateAuth(MemberRoleEnum.ADMIN);
            memberRepository.save(member.get());

        } else {
            return new ResponseEntity<>("잘못된 접근입니다.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("관리자 설정 완료", HttpStatus.OK);
    }

}
