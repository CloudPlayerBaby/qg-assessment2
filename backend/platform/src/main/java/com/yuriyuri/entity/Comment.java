package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_comment")
public class Comment {
    @TableId
    private Long id;

    private Long postId;

    private String postType; // "lost" or "found"

    private Long senderId;

    private Long receiverId;

    private String content;

    private String contactInfo;

    private Integer status; // 0: unread, 1: read

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
