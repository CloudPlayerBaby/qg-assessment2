package com.yuriyuri.dto.found;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoundInfoRequest {
    @NotEmpty(message = "物品名称不能为空")
    private String itemName;
    @NotEmpty(message = "拾取地点不能为空")
    private String foundPlace;
    @NotNull(message = "拾取时间不能为空")
    private LocalDateTime foundTime;
    @NotEmpty(message = "物品描述不能为空")
    private String description;
    @NotEmpty(message = "联系方式不能为空")
    private String contactInfo;
    private int applyTop;
}
