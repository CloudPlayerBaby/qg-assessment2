package com.yuriyuri.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotEmpty(message = "原密码不能为空")
    private String oldPassword;

    @NotEmpty(message = "新密码不能为空")
    private String newPassword;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmPassword;
}
