package com.yuriyuri.service;

import com.yuriyuri.dto.message.PrivateChatRow;
import com.yuriyuri.dto.message.PrivateMessageSendRequest;
import com.yuriyuri.entity.PrivateMessage;

import java.util.List;

public interface PrivateMessageService {
    //流程：发送私信->session获取用户是否在线
    //1.是->推送信息->可在线聊天
    //2.否->推送信息
    //其余逻辑与CommentService一致
    void send(Long senderId, PrivateMessageSendRequest req);
    List<PrivateMessage> list(Long userId, Long postId, String postType, Long peerId);
    void markRead(Long userId, Long postId, String postType, Long peerId);
    Long countUnread(Long userId);
    List<PrivateChatRow> myChats(Long userId);
}
