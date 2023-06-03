package com.example.transportation.dto.response;

import lombok.Getter;

@Getter
public class GoogleMemberInfoDto {
    private String id;
    private String email;
    private Boolean verified_email;
    private String name;
    private String given_name;
    private String family_name;
    private String profileImgUrl;
    private String locale;

    public GoogleMemberInfoDto(String id, String email, String name, String profileImgUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

}
