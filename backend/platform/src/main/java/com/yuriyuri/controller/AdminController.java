package com.yuriyuri.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.annotation.RequireIdentity;
import com.yuriyuri.common.Result;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.admin.TopRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.Report;
import com.yuriyuri.entity.User;
import com.yuriyuri.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequireIdentity({"admin"})
@Tag(name = "管理员管理", description = "设置置顶、处理举报等接口")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/posts/lost")
    @Operation(summary = "所有用户的失物信息")
    public Result<Page<LostItem>> getLostInfoByPage(@RequestParam(required = false) String itemName,
                                                    @RequestParam(required = false) String lostPlace,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "5") int pageSize) {
        LostInfoRequest req = new LostInfoRequest();
        req.setItemName(itemName);
        req.setLostPlace(lostPlace);
        Page<LostItem> lostInfoByPage = adminService.getLostInfo(req, pageNum, pageSize);
        return Result.success(lostInfoByPage);
    }

    @GetMapping("/posts/found")
    @Operation(summary = "所有用户的拾物信息")
    public Result<Page<FoundItem>> getFoundInfoByPage(@RequestParam(required = false) String itemName,
                                                      @RequestParam(required = false) String FoundPlace,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "5") int pageSize) {
        FoundInfoRequest req = new FoundInfoRequest();
        req.setItemName(itemName);
        req.setFoundPlace(FoundPlace);
        Page<FoundItem> FoundInfoByPage = adminService.getFoundInfo(req, pageNum, pageSize);
        return Result.success(FoundInfoByPage);
    }

    @PutMapping("/posts/lost/top")
    @Operation(summary = "设置失物置顶")
    public Result<Void> setLostTop(@RequestBody TopRequest req) {
        adminService.setLostTop(req);
        return Result.success();
    }

    @PutMapping("/posts/found/top")
    @Operation(summary = "设置拾物置顶")
    public Result<Void> setFoundTop(@RequestBody TopRequest req) {
        adminService.setFoundTop(req);
        return Result.success();
    }

    @PutMapping("/posts/lost/reject")
    @Operation(summary = "拒绝失物置顶")
    public Result<Void> rejectLostTop(@RequestParam Long id) {
        adminService.rejectLostTop(id);
        return Result.success();
    }

    @PutMapping("/posts/found/reject")
    @Operation(summary = "拒绝拾物置顶")
    public Result<Void> rejectFoundTop(@RequestParam Long id) {
        adminService.rejectFoundTop(id);
        return Result.success();
    }

    @GetMapping("/report/item")
    @Operation(summary = "查看举报单")
    public Result<Page<Report>> getPostReport(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "5") int pageSize) {
        Page<Report> postReport = adminService.getPostReport(pageNum, pageSize);
        return Result.success(postReport);
    }

    @PatchMapping("/report/accept")
    @Operation(summary = "接受举报")
    public Result<Page<Report>> acceptPostReport(@RequestParam Long id,
                                                 @RequestBody(required = false) ReportRequest req) {
        adminService.acceptPostReport(id, req);
        return Result.success();
    }

    @PatchMapping("/report/reject")
    @Operation(summary = "拒绝举报")
    public Result<Page<Report>> rejectPostReport(@RequestParam Long id,
                                                 @RequestBody(required = false) ReportRequest req) {
        adminService.rejectPostReport(id, req);
        return Result.success();
    }

    @GetMapping("/admin/users")
    @Operation(summary = "管理员查看用户信息的接口")
    public Result<Page<User>> getUsers(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "5") int pageSize) {
        Page<User> userPage = adminService.selectAllUsers(pageNum, pageSize);
        return Result.success(userPage);
    }

    @PatchMapping("/admin/users/ban/{id}")
    @Operation(summary = "管理员封禁违规用户")
    public Result<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return Result.success();
    }

    @PatchMapping("/admin/users/unBan/{id}")
    @Operation(summary = "管理员封禁违规用户")
    public Result<Void> unBanUser(@PathVariable Long id) {
        adminService.unBanUser(id);
        return Result.success();
    }

    @PatchMapping("/admin/posts/ban")
    @Operation(summary = "封禁帖子")
    public Result<Void> banPost(@RequestParam Long id,
                                @RequestParam String type) {
        adminService.banPost(id, type);
        return Result.success();
    }

    @PatchMapping("/admin/posts/unBan")
    @Operation(summary = "解封帖子")
    public Result<Void> unBanPost(@RequestParam Long id,
                                  @RequestParam String type) {
        adminService.unBanPost(id, type);
        return Result.success();
    }

    @DeleteMapping("/admin/posts")
    @Operation(summary = "删除帖子")
    public Result<Void> deletePost(@RequestParam Long id,
                                   @RequestParam String type) {
        adminService.deletePost(id, type);
        return Result.success();
    }

    @GetMapping("/admin/statistics/posts-count")
    @Operation(summary = "获取发布信息数量")
    public Result<Long> getPostNumber() {
        Long postNumber = adminService.getPostNumber();
        return Result.success(postNumber);
    }

    @GetMapping("/admin/statistics/posts-completed-count")
    @Operation(summary = "获取找回物品数量")
    public Result<Long> getCompletedPostNumber() {
        Long completedPostNumber = adminService.getCompletedPostNumber();
        return Result.success(completedPostNumber);
    }

    @GetMapping("/admin/statistics/active-users-count")
    @Operation(summary = "获取活跃用户数")
    public Result<Long> getActiveUsersNumber() {
        //开始时间是前一天，结束时间是现在
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        Long activeUsersNumber = adminService.getActiveUsersNumber(startTime);
        return Result.success(activeUsersNumber);
    }

    @Autowired
    private com.yuriyuri.service.AiService aiService;

    @GetMapping("/admin/statistics/ai-analysis")
    @Operation(summary = "获取AI数据分析报告")
    public Result<String> getAiAnalysisReport() {
        //开始时间是一周前，结束时间是现在
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> lostPlaceStatistics = adminService.getLostPlaceStatistics(startTime);
        List<Map<String, Object>> lostItemStatistics = adminService.getLostItemStatistics(startTime);
        List<Map<String, Object>> foundPlaceStatistics = adminService.getFoundPlaceStatistics(startTime);
        List<Map<String, Object>> foundItemStatistics = adminService.getFoundItemStatistics(startTime);

        String report = aiService.generateAnalysisReport(
                lostPlaceStatistics, lostItemStatistics, foundPlaceStatistics, foundItemStatistics);
        return Result.success(report);
    }

}


