package com.yuriyuri.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class ResetPasswordRequest {

    @Email(message = "邮箱格式不正确")
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @NotEmpty(message = "验证码不能为空")
    private String captcha;

    @NotEmpty(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    private String newPassword;

    @NotEmpty(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    private String confirmPassword;
}
