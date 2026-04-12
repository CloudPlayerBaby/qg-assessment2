package com.yuriyuri.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.LostItem;

public interface LostService {
    void postLostInfo(Long userId, LostInfoRequest req);
    Page<LostItem> getLostInfo(Long userId,int pageNum,int pageSize);
    LostItem getLostInfoOne(Long id);
    void updateLostInfo(Long id, Long userId, LostInfoRequest req);
    void deleteLostInfo(Long id, Long userId);
    Page<LostItem> getLostInfoByPage (LostInfoRequest req, int pageNum, int pageSize);
    void reportPost(Long userId,ReportRequest req);
    void confirmItem(Long id, Long userId);
}
