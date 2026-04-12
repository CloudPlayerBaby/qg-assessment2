package com.yuriyuri.service;

import com.yuriyuri.dto.message.PrivateMessageVO;
import com.yuriyuri.dto.message.PrivateSessionRequest;
import com.yuriyuri.dto.message.PrivateSessionSummaryVO;
import com.yuriyuri.dto.message.PrivateSessionVO;

import java.util.List;

public interface PrivateMessageService {

    PrivateSessionVO ensureSession(Long userId, PrivateSessionRequest req);

    List<PrivateMessageVO> listHistory(Long userId, String sessionId);

    void markSessionRead(Long userId, String sessionId);

    Long countUnread(Long userId);

    List<PrivateSessionSummaryVO> listSessionSummaries(Long userId);

    PrivateMessageVO sendMessage(Long senderId, String sessionId, String content);
}
