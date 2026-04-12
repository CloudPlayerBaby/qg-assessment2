package com.yuriyuri.dto.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private String postType; // "lost" or "found"
    private String content;
    private String contactInfo;
}
