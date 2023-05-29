package com.example.transportation.dto.user;

import com.example.transportation.entity.Member;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class MemberDto implements Serializable {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class delete {
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class register {
        private Long id;
        private String name;
        private String email;
        private String password;
        private String profileImageUrl;
    }

    @Data
    @Builder
    public static class registerResponse {
        private final String name;
        private final String email;
        private final String atk;
        private final String rtk;

        public static registerResponse response(String name, String email, String atk, String rtk) {
            return registerResponse.builder()
                    .email(email)
                    .name(name)
                    .atk(atk)
                    .rtk(rtk)
                    .build();
        }
    }

    @Data
    @Builder
    public static class login {
        private final String password;
        private final String email;
    }

    @Data
    @Builder
    public static class reissue {
        private final String password;
        private final String email;
        private final String rtk;
    }

    @Data
    @Builder
    public static class loginResponse {
        private final String atk;
        private final String rtk;

        public static loginResponse response(String atk, String rtk) {
            return loginResponse.builder()
                    .atk(atk)
                    .rtk(rtk)
                    .build();
        }
    }

    @Data
    @Builder
    public static class socialLoginResponse {
        private final String status;
        private final String name;
        private final String email;
        private final String profileImageUrl;
        private final String atk;
        private final String rtk;

        public static socialLoginResponse response(String name, String email, String profileImageUrl, String atk, String rtk, String status) {
            return socialLoginResponse.builder()
                    .status(status)
                    .name(name)
                    .email(email)
                    .profileImageUrl(profileImageUrl)
                    .atk(atk)
                    .rtk(rtk)
                    .build();
        }
    }

    @Data
    @Builder
    public static class infoResponse {
        private Long id;
        private String name;
        private String email;
        private String profileImageUrl;

        public static infoResponse response(@NotNull Member member) {
            return infoResponse.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .profileImageUrl(member.getProfileImageUrl())
                    .build();
        }
    }
}
