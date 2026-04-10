package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId
    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phoneNumber;

    private String avatarUrl;

    private String password;

    private Integer identity;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
