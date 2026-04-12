package com.yuriyuri.dto.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateSessionSummaryVO {
    private String sessionId;
    private Long postId;
    private String postType;
    private Long peerId;
    private String peerDisplayName;
    private String lastContent;
    private LocalDateTime lastTime;
    private Long unreadCount;
}
