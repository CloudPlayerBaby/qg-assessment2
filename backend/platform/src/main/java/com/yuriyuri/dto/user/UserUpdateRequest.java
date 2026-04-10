package com.yuriyuri.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    //头像可选，没有约束条件
    private String avatarUrl;
}
