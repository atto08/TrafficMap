package com.example.transportation.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class KakaoUserInfoDto {
    private Long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public class Properties {
        private String nickname;
        private String profile_image;
        private String thumbnail_image;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public class KakaoAccount {
        private String email;
        private Boolean profile_nickname_needs_agreement;
        private Boolean profile_image_needs_agreement;
        private profile profile;
        private Boolean name_needs_agreement;
        private Boolean email_needs_agreement;
        private Boolean has_email;
        private Boolean is_email_valid;
        private Boolean is_email_verified;

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        public class profile {
            private String nickname;
            private String thumbnail_image_url;
            private String profile_image_url;
            private Boolean is_default_image;
        }
    }
}
