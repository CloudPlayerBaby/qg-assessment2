package com.yuriyuri.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserInfo userInfo;

    //相当于嵌套了一个实体类，便于返回登陆注册时的信息
    @Data
    //无参构造方法
    @NoArgsConstructor
    //有参构造方法
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String phoneNumber;
        private String avatarUrl;
        private Integer identity;
    }
}
