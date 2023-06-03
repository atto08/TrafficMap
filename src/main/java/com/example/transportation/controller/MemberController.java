package com.example.transportation.controller;

import com.example.transportation.security.MemberDetailsImpl;
import com.example.transportation.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping(value = "/google/code")
    public void getGoogleCode(HttpServletResponse response) throws Exception {
        response.sendRedirect(memberService.getGoogleCode());
    }


    @GetMapping(value = "/google/login")
    public ResponseEntity<?> googleLogin(@RequestParam String code) throws JsonProcessingException {
        return memberService.googleLogin(code);
    }


    @GetMapping(value = "/admin")
    public ResponseEntity<?> getAdmin(@AuthenticationPrincipal MemberDetailsImpl memberDetails, @RequestParam String adminToken) {
        return memberService.getAdmin(memberDetails.getMember().getEmail(), memberDetails.getMember().getGoogleId(), adminToken);
    }


//    @PostMapping(value = "/signup")
//    public ResponseEntity<?> signup(@RequestBody SignupRequestDto signupRequestDto) {
//        return memberService.signup(signupRequestDto);
//    }
//
//    @GetMapping(value = "/login")
//    public ResponseEntity<?> login() {
//        return memberService
//    }
}
