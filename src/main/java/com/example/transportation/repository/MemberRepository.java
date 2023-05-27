package com.example.transportation.repository;

import com.example.transportation.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findOneWithAuthoritiesByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);

    Optional<Member> findSellerById(Long userId);
}