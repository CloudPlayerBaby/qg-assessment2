package com.yuriyuri.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrivateMessageSendRequest {
    @NotNull
    private Long postId;

    @NotBlank
    private String postType;

    @NotNull
    private Long receiverId;

    @NotBlank
    private String content;
}
