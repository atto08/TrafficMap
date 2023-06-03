package com.example.transportation.repository;

import com.example.transportation.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,String> {

    Optional<Member> findByName(String name);

    Optional<Member> findByGoogleId(String googleId);

    Optional<Member> findByEmailAndGoogleId(String email, String googleId);

}
