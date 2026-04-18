package com.yuriyuri.util;

import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 上下文格式化工具
 * 减少传递给 AI 的上下文数据量
 */
public class AiContextUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    /**
     * 格式化拾物记录列表（精简字段）
     */
    public static String formatFoundItems(List<FoundItem> items) {
        if (items == null || items.isEmpty()) {
            return "暂无拾物记录";
        }
        return items.stream()
                .limit(20) // 最多取20条，避免上下文过长
                .map(item -> String.format(
                        "[%d]%s|%s|%s|%s",
                        item.getId(),
                        truncate(item.getItemName(), 10),
                        truncate(item.getFoundPlace(), 10),
                        formatDate(item.getFoundTime()),
                        truncate(item.getDescription(), 30)
                ))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化失物记录列表（精简字段）
     */
    public static String formatLostItems(List<LostItem> items) {
        if (items == null || items.isEmpty()) {
            return "暂无失物记录";
        }
        return items.stream()
                .limit(20) // 最多取20条
                .map(item -> String.format(
                        "[%d]%s|%s|%s|%s",
                        item.getId(),
                        truncate(item.getItemName(), 10),
                        truncate(item.getLostPlace(), 10),
                        formatDate(item.getLostTime()),
                        truncate(item.getDescription(), 30)
                ))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 格式化统计数据
     */
    public static String formatStatistics(String type, List<Map<String, Object>> stats) {
        if (stats == null || stats.isEmpty()) {
            return type + ": 无数据";
        }
        return stats.stream()
                .limit(5) // 只取前5
                .map(map -> {
                    String name = map.getOrDefault("location", map.get("item")).toString();
                    Object count = map.get("count");
                    return truncate(name, 10) + ":" + count;
                })
                .collect(Collectors.joining(", ", type + "[", "]"));
    }

    /**
     * 截断字符串
     */
    private static String truncate(String str, int maxLength) {
        if (str == null || str.isEmpty()) {
            return "-";
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }

    /**
     * 格式化日期
     */
    private static String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
