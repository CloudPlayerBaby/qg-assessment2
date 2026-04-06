package com.yuriyuri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotEmpty(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式不正确")
    @NotEmpty(message = "邮箱不能为空")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmPassword;
}
