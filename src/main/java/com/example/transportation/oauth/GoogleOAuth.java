package com.example.transportation.oauth;

import com.example.transportation.dto.oauth.GoogleOAuthTokenDto;
import com.example.transportation.dto.user.GoogleUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleOAuth {

    private final String GOOGLE_TOKEN_REQUEST_URL = "https://oauth2.googleapis.com/token";
    private final String GOOGLE_USERINFO_REQUEST_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final String googleLoginUrl = "https://accounts.google.com";
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    @Value("${app.google.clientid}")
    private String googleClientId;
    @Value("${app.google.redirectUrl}")
    private String googleRedirectUrl;
    @Value("${app.google.clientSecret}")
    private String googleClientSecret;

    public String getOauthRedirectURL() {
        String reqUrl = googleLoginUrl + "/o/oauth2/v2/auth?client_id=" + googleClientId + "&redirect_uri=" + googleRedirectUrl
                + "&response_type=code&scope=email%20profile%20openid&access_type=offline";
        return reqUrl;
    }

    public ResponseEntity<String> requestAccessToken(String code) {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUrl);
        params.add("grant_type", "authorization_code");

        // 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(GOOGLE_TOKEN_REQUEST_URL, HttpMethod.POST, requestEntity, String.class);

        return responseEntity;
    }

    public GoogleOAuthTokenDto getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        // 응답결과를 GoogleOAuthTokenDto 형식으로 리턴
        GoogleOAuthTokenDto googleOAuthTokenDto = objectMapper.readValue(response.getBody(), GoogleOAuthTokenDto.class);
        return googleOAuthTokenDto;
    }

    public ResponseEntity<String> requestUserInfo(GoogleOAuthTokenDto oAuthToken) {
        // Http Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        // Http 헤더에 Authorization 을 담아서 요청
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
        return response;
    }

    public GoogleUserInfoDto getUserInfo(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 응답결과를 GoogleUserInfoDto 형식으로 리턴
        GoogleUserInfoDto googleUserInfoDto = objectMapper.readValue(response.getBody(), GoogleUserInfoDto.class);
        return googleUserInfoDto;
    }

}
