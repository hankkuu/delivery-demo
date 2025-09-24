package com.barogo.delivery.api.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 요청 DTO (record로 불변 형태 + Bean Validation)
public record SignUpRequest(
        @NotBlank
        @Size(max = 50)
        String loginId,

        @NotBlank
        @Size(min = 12, max = 255)
        @Pattern(regexp = PASSWORD_RULE, message = "비밀번호는 대문자/소문자/숫자/특수문자 중 3종류 이상을 포함하여 12자 이상이어야 합니다.")
        String password,

        @NotBlank
        @Size(max = 50)
        String name
) {
    // 비밀번호 규칙: 12자 이상 + (대문자/소문자/숫자/특수문자) 중 최소 3종류 포함
    private static final String PASSWORD_RULE =
            "^(?=.{12,})(?:(?=(?:.*[A-Z]))(?=(?:.*[a-z]))(?=(?:.*\\d))|"
                    + "(?=(?:.*[A-Z]))(?=(?:.*[a-z]))(?=(?:.*[^A-Za-z\\d]))|"
                    + "(?=(?:.*[A-Z]))(?=(?:.*\\d))(?=(?:.*[^A-Za-z\\d]))|"
                    + "(?=(?:.*[a-z]))(?=(?:.*\\d))(?=(?:.*[^A-Za-z\\d]))).*$";
}
