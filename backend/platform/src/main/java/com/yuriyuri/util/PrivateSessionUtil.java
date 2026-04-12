package com.yuriyuri.util;

import com.yuriyuri.common.BusinessException;

public final class PrivateSessionUtil {

    private PrivateSessionUtil() {
    }

    public static String buildSessionId(String postType, long postId, long userA, long userB) {
        long min = Math.min(userA, userB);
        long max = Math.max(userA, userB);
        return postType + "_" + postId + "_" + min + "_" + max;
    }

    public static boolean validShape(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        String[] p = sessionId.split("_");
        if (p.length != 4) {
            return false;
        }
        if (!"lost".equals(p[0]) && !"found".equals(p[0])) {
            return false;
        }
        return digitsOnly(p[1]) && digitsOnly(p[2]) && digitsOnly(p[3]);
    }

    private static boolean digitsOnly(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void validateSessionShape(String sessionId) {
        if (!validShape(sessionId)) {
            throw new BusinessException("无效的会话");
        }
    }

    public static boolean userInSession(String sessionId, long userId) {
        if (!validShape(sessionId)) {
            return false;
        }
        String[] p = sessionId.split("_");
        long u1 = Long.parseLong(p[2]);
        long u2 = Long.parseLong(p[3]);
        return userId == u1 || userId == u2;
    }

    public static long getPeerUserId(String sessionId, long senderId) {
        String[] p = sessionId.split("_");
        long u1 = Long.parseLong(p[2]);
        long u2 = Long.parseLong(p[3]);
        if (senderId == u1) {
            return u2;
        }
        if (senderId == u2) {
            return u1;
        }
        throw new BusinessException("您不在该会话中");
    }

    public static long getPostId(String sessionId) {
        return Long.parseLong(sessionId.split("_")[1]);
    }

    public static String getPostType(String sessionId) {
        return sessionId.split("_")[0];
    }
}
