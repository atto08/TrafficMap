package com.example.transportation.entity;

import com.example.transportation.util.TimeStamped;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member extends TimeStamped {

    @Id
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    private String profileImgUrl;

    @Column(nullable = false)
    private MemberRoleEnum authority;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<SubwayRouteBookmark> subwayRouteBookmarkList;

    public Member(String email,  String googleId, String name, String encodedPassword, String profileImgUrl, MemberRoleEnum role) {
        this.email = email;
        this.googleId = googleId;
        this.name = name;
        this.password = encodedPassword;
        this.profileImgUrl = profileImgUrl;
        this.authority = role;

    }

    public void updateAuth (MemberRoleEnum role) {
        this.authority = role;
    }

}
