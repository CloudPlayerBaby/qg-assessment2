package com.yuriyuri.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.entity.FoundItem;

public interface FoundService {
    void postFoundInfo(Long userId, FoundInfoRequest req);
    Page<FoundItem> getFoundInfo(Long userId, int pageNum, int pageSize);
    FoundItem getFoundInfoOne(Long id);
    void updateFoundInfo(Long id, Long userId, FoundInfoRequest req);
    void deleteFoundInfo(Long id, Long userId);
    Page<FoundItem> getFoundInfoByPage(FoundInfoRequest req, int pageNum, int pageSize);
    void reportPost(Long userId, ReportRequest req);
}
