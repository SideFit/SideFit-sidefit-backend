package com.project.sidefit.api.dto.sign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrevRequestDto {

    // 전부 필수값

    @Email
    private String email;

    // 6글자 이상
    private String password;

    private String passwordCheck;
}
