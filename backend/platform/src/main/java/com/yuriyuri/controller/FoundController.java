package com.yuriyuri.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.common.Result;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.service.FoundService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/found")
@Tag(name = "拾取物品管理", description = "发布拾物信息、更改拾物信息等接口")
public class FoundController {
    @Autowired
    private FoundService foundService;

    @PostMapping("/item")
    @Operation(summary = "发布拾取物品")
    public Result<Void> postFoundItem(@Valid @RequestBody FoundInfoRequest foundInfoRequest) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long id = ((Number) map.get("id")).longValue();
        foundService.postFoundInfo(id, foundInfoRequest);
        return Result.success();
    }

    @GetMapping("/mine")
    @Operation(summary = "用户查看自己发布的信息")
    public Result<Page<FoundItem>> getFoundInfo(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "5") int pageSize) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        Page<FoundItem> foundInfo = foundService.getFoundInfo(userId, pageNum, pageSize);
        return Result.success(foundInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户查看某单个拾物信息的详情")
    public Result<FoundItem> getFoundInfoOne(@PathVariable Long id) {
        FoundItem foundInfoOne = foundService.getFoundInfoOne(id);
        return Result.success(foundInfoOne);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "用户更新自己的拾物信息")
    public Result<Void> updateFoundInfo(@PathVariable Long id,
                                        @Valid @RequestBody FoundInfoRequest req) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        foundService.updateFoundInfo(id, userId, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "用户删除自己的拾物信息")
    public Result<Void> deleteFoundInfo(@PathVariable Long id) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        foundService.deleteFoundInfo(id, userId);
        return Result.success();
    }

    @PutMapping("/report/item")
    @Operation(summary = "举报拾物帖子")
    public Result<Void> reportItem(@Valid @RequestBody ReportRequest req) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        foundService.reportPost(userId, req);
        return Result.success();
    }

    @GetMapping("/home")
    @Operation(summary = "所有拾物帖子")
    public Result<Page<FoundItem>> getFoundInfoByPage(@RequestParam(required = false) String itemName,
                                                       @RequestParam(required = false) String foundPlace,
                                                       @RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "5") int pageSize) {
        FoundInfoRequest req = new FoundInfoRequest();
        req.setItemName(itemName);
        req.setFoundPlace(foundPlace);
        Page<FoundItem> foundInfoByPage = foundService.getFoundInfoByPage(req, pageNum, pageSize);
        return Result.success(foundInfoByPage);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "确认拾物已认领")
    public Result<Void> confirmFoundItem(@PathVariable Long id){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        foundService.confirmItem(id, userId);
        return Result.success();
    }
}
