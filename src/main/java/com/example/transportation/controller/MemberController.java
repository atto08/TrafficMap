package com.example.transportation.controller;

import com.example.transportation.dto.user.MemberDto;
import com.example.transportation.service.MemberService;
import com.example.transportation.utils.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<MemberDto.loginResponse> login(
            @RequestBody MemberDto.login request
    ) {
        return memberService.login(request);
    }

    // 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<MemberDto.registerResponse> register(
            @RequestBody MemberDto.register request
    ) {
        return memberService.register(request);
    }

    // 로그인 만료시 atk 재발급
    @GetMapping
    public ResponseEntity<MemberDto.loginResponse> reissue(
            @RequestHeader(value = "REFRESH_TOKEN") String rtk
    ) {
        return memberService.reissue(rtk);
    }

    // 로그아웃
    @PatchMapping("/auth/logout")
    @PreAuthorize("hasAnyRole('ADMIN','USER','OFFICE')")
    public ResponseEntity<Status> logout(
            @RequestHeader(value = "Authorization") String auth
    ) {
        return memberService.logout(auth);
    }
}
