package com.yuriyuri.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心「私聊」列表里每一行：和谁在哪个帖子下聊、最后一句、未读数
 */
@Data
public class PrivateChatRow {
    private Long postId;
    private String postType;
    private Long peerId;
    private String peerName;
    private String lastContent;
    private LocalDateTime lastTime;
    private Long unreadCount;
}
