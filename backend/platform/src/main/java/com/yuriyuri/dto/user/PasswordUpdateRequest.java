package com.yuriyuri.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotEmpty(message = "原密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    private String oldPassword;

    @NotEmpty(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    private String newPassword;

    @NotEmpty(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    private String confirmPassword;
}
