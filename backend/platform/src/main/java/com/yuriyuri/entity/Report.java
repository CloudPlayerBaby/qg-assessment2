package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Report {
    private Long id;
    //举报的用户id
    private Long userId;
    //举报的表，需要id和类型，因为失物表和拾物表是两个表
    private Long targetId;
    @TableField("target_type")
    private String type;

    private String reason;
    private int status;
    private LocalDateTime createTime;
}
