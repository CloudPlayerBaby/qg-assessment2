package com.yuriyuri.dto.admin;

import lombok.Data;

@Data
public class ReportRequest {
    private Long targetId;
    private String type;
    private String reason;
}
