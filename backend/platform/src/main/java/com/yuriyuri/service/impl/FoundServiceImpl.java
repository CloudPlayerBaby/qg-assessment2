package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.post.PostSortOrder;
import com.yuriyuri.constant.post.PostStatus;
import com.yuriyuri.constant.report.ReportStatus;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.Report;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.ReportMapper;
import com.yuriyuri.service.FoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoundServiceImpl implements FoundService {
    @Autowired
    private FoundMapper foundMapper;

    @Autowired
    private ReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postFoundInfo(Long userId, FoundInfoRequest req) {
        FoundItem foundItem = new FoundItem();
        foundItem.setUserId(userId);
        foundItem.setItemName(req.getItemName());
        foundItem.setFoundPlace(req.getFoundPlace());
        foundItem.setFoundTime(req.getFoundTime());
        foundItem.setDescription(req.getDescription());
        foundItem.setContactInfo(req.getContactInfo());
        foundItem.setSortOrder(PostSortOrder.NORMAL);
        foundItem.setApplyTop(req.getApplyTop());
        foundItem.setStatus(PostStatus.NORMAL);
        foundMapper.insert(foundItem);
    }

    @Override
    public Page<FoundItem> getFoundInfo(Long userId, int pageNum, int pageSize) {
        Page<FoundItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoundItem::getUserId, userId);
        wrapper.orderByDesc(FoundItem::getCreateTime);
        return foundMapper.selectPage(page, wrapper);
    }

    @Override
    @Cacheable(value = "foundItem", key = "#id")
    public FoundItem getFoundInfoOne(Long id) {
        LambdaQueryWrapper<FoundItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoundItem::getId, id);
        return foundMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "foundItem", key = "#id")
    public void updateFoundInfo(Long id, Long userId, FoundInfoRequest req) {
        FoundItem foundItem = checkInfoExist(id);

        if (!foundItem.getUserId().equals(userId)) {
            throw new BusinessException("无权修改他人的拾物信息");
        }

        LambdaUpdateWrapper<FoundItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(FoundItem::getId, id)
                .set(req.getItemName() != null, FoundItem::getItemName, req.getItemName())
                .set(req.getFoundPlace() != null, FoundItem::getFoundPlace, req.getFoundPlace())
                .set(req.getFoundTime() != null, FoundItem::getFoundTime, req.getFoundTime())
                .set(req.getDescription() != null, FoundItem::getDescription, req.getDescription())
                .set(req.getContactInfo() != null, FoundItem::getContactInfo, req.getContactInfo());

        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            throw new BusinessException("信息未更新");
        }
        foundMapper.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "foundItem", key = "#id")
    public void deleteFoundInfo(Long id, Long userId) {
        FoundItem foundItem = checkInfoExist(id);

        if (!foundItem.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人的拾物信息");
        }

        foundMapper.deleteById(id);
    }

    @Override
    public Page<FoundItem> getFoundInfoByPage(FoundInfoRequest req, int pageNum, int pageSize) {
        Page<FoundItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(req.getItemName() != null, FoundItem::getItemName, req.getItemName())
                .like(req.getFoundPlace() != null, FoundItem::getFoundPlace, req.getFoundPlace())
                .in(FoundItem::getStatus, PostStatus.REQUESTED, PostStatus.NORMAL)
                .orderByDesc(FoundItem::getSortOrder)
                .orderByDesc(FoundItem::getUpdateTime);

        Page<FoundItem> foundItemPage = foundMapper.selectPage(page, wrapper);
        if (foundItemPage.getTotal() <= 0) {
            return null;
        }

        return foundItemPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportPost(Long userId, ReportRequest req) {
        String type = req.getType();
        if (!"found".equals(type)) {
            throw new BusinessException("不是拾物贴");
        }

        Long targetId = req.getTargetId();
        if (foundMapper.selectById(targetId) == null) {
            throw new BusinessException("帖子不存在");
        }

        String reason = req.getReason();
        if (reason == null) {
            throw new BusinessException("原因不能为空");
        }

        Report report = new Report();
        report.setUserId(userId);
        report.setTargetId(targetId);
        report.setType(type);
        report.setReason(reason);
        report.setStatus(ReportStatus.REQUESTED);
        reportMapper.insert(report);

        LambdaUpdateWrapper<FoundItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FoundItem::getId, targetId)
                .set(FoundItem::getStatus, PostStatus.REQUESTED);
        foundMapper.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmItem(Long id, Long userId) {
        FoundItem foundItem = checkInfoExist(id);

        if (!foundItem.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人的拾物信息");
        }

        LambdaUpdateWrapper<FoundItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FoundItem::getId, id)
                .set(FoundItem::getStatus, PostStatus.FINISHED);
        foundMapper.update(updateWrapper);
    }

    @Cacheable(value = "lostItem", key = "#id")
    public FoundItem checkInfoExist(Long id) {
        LambdaQueryWrapper<FoundItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FoundItem::getId, id);
        FoundItem foundItem = foundMapper.selectOne(queryWrapper);

        if (foundItem == null) {
            throw new BusinessException("拾物信息不存在");
        }
        return foundItem;
    }
}
