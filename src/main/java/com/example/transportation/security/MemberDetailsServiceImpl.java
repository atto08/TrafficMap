package com.example.transportation.security;

import com.example.transportation.entity.Member;
import com.example.transportation.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsServiceImpl {

    private final MemberRepository memberRepository;

    public MemberDetailsImpl loadMember(String id) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("회원가입이 필요합니다."));
        return new MemberDetailsImpl(member);
    }
}
