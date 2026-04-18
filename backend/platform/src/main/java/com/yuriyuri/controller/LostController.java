package com.yuriyuri.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.common.Result;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.service.LostService;
import com.yuriyuri.util.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/lost")
@Tag(name="丢失物品管理",description = "发布失物信息、更改失物信息等接口")
public class LostController {
    @Autowired
    private LostService lostService;

    @PostMapping("/item")
    @Operation(summary = "发布丢失物品")
    public Result<Void> postLostItem(@Valid @RequestBody LostInfoRequest lostInfoRequest){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long id = ((Number) map.get("id")).longValue();
        lostService.postLostInfo(id, lostInfoRequest);
        return Result.success();
    }

    @GetMapping("/mine")
    @Operation(summary = "用户查看自己发布的信息")
    public Result<Page<LostItem>> getLostInfo(@RequestParam(defaultValue = "1")int pageNum,
                                              @RequestParam(defaultValue = "5")int pageSize){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        Page<LostItem> lostInfo = lostService.getLostInfo(userId, pageNum, pageSize);
        return Result.success(lostInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户查看某单个失物信息的详情")
    public Result<LostItem> getLostInfoOne(@PathVariable Long id){
        //查看失物详情，不需要是自己的
        LostItem lostInfoOne = lostService.getLostInfoOne(id);
        if (lostInfoOne == null) {
            throw new BusinessException("失物信息不存在");
        }
        return Result.success(lostInfoOne);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "用户更新自己的失物信息")
    public Result<Void> updateLostInfo(@PathVariable Long id,
                                       @Valid @RequestBody LostInfoRequest req){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        lostService.updateLostInfo(id, userId, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "用户删除自己的失物信息")
    public Result<Void> deleteLostInfo(@PathVariable Long id){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        lostService.deleteLostInfo(id, userId);
        return Result.success();
    }

    @PutMapping("/report/item")
    @Operation(summary = "举报失物帖子")
    public Result<Void> reportItem(@Valid @RequestBody ReportRequest req){
        //获取举报的用户的id
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        lostService.reportPost(userId, req);
        return Result.success();
    }

    @GetMapping("/home")
    @Operation(summary = "所有失物帖子")
    public Result<Page<LostItem>> getLostInfoByPage(@RequestParam(required = false) String itemName,
                                                    @RequestParam(required = false) String lostPlace,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "5") int pageSize){
        LostInfoRequest req = new LostInfoRequest();
        req.setItemName(itemName);
        req.setLostPlace(lostPlace);
        Page<LostItem> lostInfoByPage = lostService.getLostInfoByPage(req, pageNum, pageSize);
        return Result.success(lostInfoByPage);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "确认失物已认领")
    public Result<Void> confirmLostItem(@PathVariable Long id){
        Map<String,Object> map = ThreadLocalUtil.get();
        Long userId = ((Number) map.get("id")).longValue();
        lostService.confirmItem(id, userId);
        return Result.success();
    }
}
