package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoundItem {
    @TableId
    private Long id;
    private Long userId;
    private String itemName;
    private String foundPlace;
    private LocalDateTime foundTime;
    private String description;
    private String contactInfo;
    private int sortOrder;
    private int status;
    private int applyTop;
    private LocalDateTime topEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
