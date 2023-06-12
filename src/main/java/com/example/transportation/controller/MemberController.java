package com.example.transportation.controller;

import com.example.transportation.dto.response.ResCode;
import com.example.transportation.security.MemberDetailsImpl;
import com.example.transportation.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    private final HttpHeaders headers = new HttpHeaders();

//    @GetMapping(value = "/google/code")
//    public void getGoogleCode(HttpServletResponse response) throws Exception {
//        response.sendRedirect(memberService.getGoogleCode());
//    }


    @GetMapping(value = "/auth/google/callback")
    public ResponseEntity<?> getGoogleCallback(@RequestParam String code) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/loading.html");
        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(htmlContent);
    }



    @GetMapping(value = "/members/google/login")
    public ResponseEntity<?> googleLogin(@RequestParam String code) throws JsonProcessingException {
        return memberService.googleLogin(code);
    }


    @GetMapping(value = "/members/admin")
    public ResponseEntity<?> getAdmin(@AuthenticationPrincipal MemberDetailsImpl memberDetails, @RequestParam String adminToken) {
        return memberService.getAdmin(memberDetails.getMember().getEmail(), memberDetails.getMember().getGoogleId(), adminToken);
    }

}
