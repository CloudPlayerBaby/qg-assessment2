package com.yuriyuri.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLoginLog {
    private Long id;
    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;
}
