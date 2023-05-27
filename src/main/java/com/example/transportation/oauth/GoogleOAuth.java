package com.example.transportation.oauth;

import com.example.transportation.dto.oauth.GoogleOAuthTokenDto;
import com.example.transportation.dto.user.GoogleUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

        System.out.println("code = " + params.get("code"));
        System.out.println("redirect_uri = " + params.get("redirect_uri"));

        // 요청 객체 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        System.out.println("requestEntity = " + requestEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(GOOGLE_TOKEN_REQUEST_URL, HttpMethod.POST, requestEntity, String.class);
        System.out.println("Try Response = " + responseEntity);
        System.out.println("Try responseEntity.getBody() = " + responseEntity.getBody());

        return responseEntity;

//        // POST 요청 보내기
//        try {
//            ResponseEntity<String> responseEntity = restTemplate.exchange(
//                    GOOGLE_TOKEN_REQUEST_URL,
//                    HttpMethod.POST,
//                    requestEntity,
//                    String.class
//            );
//            System.out.println("Try Response = " + responseEntity);
//            System.out.println("Try responseEntity.getBody() = " + responseEntity.getBody());
//            return responseEntity;
//        } catch (HttpClientErrorException e) {
//            ResponseEntity<String> responseEntity = ResponseEntity
//                    .status(e.getStatusCode())
//                    .body(e.getResponseBodyAsString());
//            System.out.println("Catch Response = " + responseEntity);
//            System.out.println("Catch responseEntity.getBody() = " + responseEntity.getBody());
//            return responseEntity;
//        }
    }

    public GoogleOAuthTokenDto getAccessToken(ResponseEntity<String> response) throws JsonProcessingException {
        System.out.println("Get AccessToken: response.getBody() = " + response.getBody());
        GoogleOAuthTokenDto googleOAuthTokenDto = objectMapper.readValue(response.getBody(), GoogleOAuthTokenDto.class);
        return googleOAuthTokenDto;
    }

    public ResponseEntity<String> requestUserInfo(GoogleOAuthTokenDto oAuthToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + oAuthToken.getAccess_token());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(GOOGLE_USERINFO_REQUEST_URL, HttpMethod.GET, request, String.class);
        System.out.println("response.getBody() = " + response.getBody());
        return response;
    }

    public GoogleUserInfoDto getUserInfo(ResponseEntity<String> response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleUserInfoDto googleUserInfoDto = objectMapper.readValue(response.getBody(), GoogleUserInfoDto.class);
        return googleUserInfoDto;
    }

}
