package com.yuriyuri.dto.lost;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostInfoRequest {
    @NotEmpty(message = "物品名称不能为空")
    private String itemName;
    @NotEmpty(message = "丢失地点不能为空")
    private String lostPlace;
    @NotNull(message = "丢失时间不能为空")
    private LocalDateTime lostTime;
    @NotEmpty(message = "物品描述不能为空")
    private String description;

    //图片是可选上传的
    private String imageUrl;
    private int applyTop;
}
