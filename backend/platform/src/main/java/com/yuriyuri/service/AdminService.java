package com.yuriyuri.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.admin.TopRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.Report;
import com.yuriyuri.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {
    //处理置顶内容
    //流程：看到消息->查看帖子->确认置顶/拒绝置顶
    //查看置顶信息
    Page<LostItem> getLostInfo(LostInfoRequest req, int pageNum, int pageSize);
    Page<FoundItem> getFoundInfo(FoundInfoRequest req, int pageNum, int pageSize);

    //确认置顶
    void setLostTop(TopRequest req);
    void setFoundTop(TopRequest req);
    //拒绝置顶
    void rejectLostTop(Long id);
    void rejectFoundTop(Long id);


    //处理举报内容
    //流程：看到消息->查看帖子->接受举报（同时把帖子封禁）/拒绝举报（帖子状态变回0）
    //查看举报内容
    Page<Report> getPostReport(int pageNum, int pageSize);

    //接受举报
    void acceptPostReport(Long id,ReportRequest req);
    //拒绝举报
    void rejectPostReport(Long id,ReportRequest req);

    //查看用户信息、封禁/解封违规用户
    //查看用户信息
    Page<User> selectAllUsers(int pageNum, int pageSize);
    //封禁用户/解禁用户
    void banUser(Long id);
    void unBanUser(Long id);

    //删除虚假信息、不合适内容（无需经过举报）、封禁、解封帖子
    void unBanPost(Long id,String type);
    void banPost(Long id,String type);

    void deletePost(Long id,String type);

    //平台统计
    //发布信息数量、找回物品数量、活跃用户数
    Long getPostNumber();
    Long getCompletedPostNumber();
    Long getActiveUsersNumber(LocalDateTime startTime);

    //获取7天内失物地点统计
    List<Map<String, Object>> getLostPlaceStatistics(LocalDateTime startTime);

    //获取7天内失物物品统计
    List<Map<String, Object>> getLostItemStatistics(LocalDateTime startTime);

    //获取7天内拾物地点统计
    List<Map<String, Object>> getFoundPlaceStatistics(LocalDateTime startTime);

    //获取7天内拾物物品统计
    List<Map<String, Object>> getFoundItemStatistics(LocalDateTime startTime);
}
