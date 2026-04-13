package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_suggestion")
public class AiSuggestion {
    @TableId
    private Long id;
    private Long userId;
    private String description;
    private String suggestion;
    private LocalDateTime createTime;
}
