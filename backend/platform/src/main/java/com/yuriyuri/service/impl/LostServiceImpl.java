package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.constant.post.PostSortOrder;
import com.yuriyuri.constant.post.PostStatus;
import com.yuriyuri.constant.report.ReportStatus;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.entity.Report;
import com.yuriyuri.mapper.LostMapper;
import com.yuriyuri.mapper.ReportMapper;
import com.yuriyuri.service.LostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LostServiceImpl implements LostService {
    @Autowired
    private LostMapper lostMapper;

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 发布失物信息
     * @param userId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postLostInfo(Long userId, LostInfoRequest req) {
        //这一整个接口都没啥好说的，就是一个简单的insert
        LostItem lostItem = new LostItem();
        lostItem.setUserId(userId);
        lostItem.setItemName(req.getItemName());
        lostItem.setLostPlace(req.getLostPlace());
        lostItem.setLostTime(req.getLostTime());
        lostItem.setDescription(req.getDescription());
        lostItem.setImageUrl(req.getImageUrl());
        //关于置顶，这里默认是0，置顶为1，具体思路在Admin那块
        lostItem.setSortOrder(PostSortOrder.NORMAL);
        //选择置顶，这里就是1，否则默认为0
        lostItem.setApplyTop(req.getApplyTop());
        //默认状态为1（正常）
        lostItem.setStatus(PostStatus.NORMAL);
        lostMapper.insert(lostItem);
    }

    /**
     * 用户查看自己发布的信息
     * @param userId
     * @return
     */
    @Override
    public Page<LostItem> getLostInfo(Long userId,int pageNum,int pageSize) {
        //进入用户主页应该看到他的所有失物信息
        Page<LostItem> page = new Page<>(pageNum,pageSize);
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        //写一个select * from lost_item where user_id=? limit(?,?)
        wrapper.eq(LostItem::getUserId,userId);
        wrapper.orderByDesc(LostItem::getCreateTime);
        return lostMapper.selectPage(page, wrapper);
    }

    /**
     * 通过id查看单个失物
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "lostItem", key = "#id") //加缓存
    public LostItem getLostInfoOne(Long id) {
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LostItem::getId, id);
        return lostMapper.selectOne(queryWrapper);
    }

    /**
     * 更新失物信息
     * @param id
     * @param userId
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "lostItem", key = "#id") //删缓存
    public void updateLostInfo(Long id, Long userId, LostInfoRequest req) {
        LostItem lostItem = checkInfoExist(id);
        
        // 校验是否是自己发布的
        if (!lostItem.getUserId().equals(userId)) {
            throw new BusinessException("无权修改他人的失物信息");
        }

        LambdaUpdateWrapper<LostItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.
                eq(LostItem::getId, id).
                set(req.getItemName()!=null,LostItem::getItemName,req.getItemName()).
                set(req.getLostPlace()!=null,LostItem::getLostPlace,req.getLostPlace()).
                set(req.getLostTime()!=null,LostItem::getLostTime,req.getLostTime()).
                set(req.getDescription()!=null,LostItem::getDescription,req.getDescription()).
                set(req.getImageUrl()!=null,LostItem::getImageUrl,req.getImageUrl());

        // 如果没有任何字段需要更新
        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            throw new BusinessException("信息未更新");
        }
        lostMapper.update(updateWrapper);
    }

    /**
     * 删除失物信息
     * @param id
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "lostItem", key = "#id")
    public void deleteLostInfo(Long id, Long userId) {
        LostItem lostItem = checkInfoExist(id);
        
        // 校验是否是自己发布的
        if (!lostItem.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人的失物信息");
        }
        
        lostMapper.deleteById(id);
    }

    /**
     * 分页查询，按时间、地点、物品名称查询
     * @param req
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<LostItem> getLostInfoByPage(LostInfoRequest req, int pageNum, int pageSize) {
        //创建分页对象
        Page<LostItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();

        //写一个select * from lost_item where item_name like ? and lost_place like ? order by sort_order desc, update_time desc limit ?,?
        wrapper.
                like(req.getItemName() != null, LostItem::getItemName, req.getItemName()).
                like(req.getLostPlace() != null, LostItem::getLostPlace, req.getLostPlace()).
                //用户首页仅能查看状态为0和1的帖子，-1被封禁首页不可查看
                in(LostItem::getStatus, PostStatus.REQUESTED,PostStatus.NORMAL).
                orderByDesc(LostItem::getSortOrder).
                orderByDesc(LostItem::getUpdateTime);

        Page<LostItem> lostItemPage = lostMapper.selectPage(page, wrapper);
        if(lostItemPage.getTotal() <= 0){
            return null;
        }

        return lostItemPage;
    }

    /**
     * 举报帖子
     * @param req
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void reportPost(Long userId,ReportRequest req) {
        //类型如果是失物贴才能举报
        String type = req.getType();
        if(!"lost".equals(type)){
            throw new BusinessException("不是失物贴");
        }

        //然后要去找对应的id
        Long targetId = req.getTargetId();
        if(lostMapper.selectById(targetId)==null){
            throw new BusinessException("帖子不存在");
        }

        String reason = req.getReason();
        if(reason==null){
            throw new BusinessException("原因不能为空");
        }
        //校验完就可以写sql了，写一个
        /*
        * insert into report
        * (user_id,target_id,target_type,reason) values(?,?,?,?)
        * */
        Report report = new Report();
        report.setUserId(userId);
        report.setTargetId(targetId);
        report.setType(type);
        report.setReason(reason);
        //默认为1，代表默认正常
        report.setStatus(ReportStatus.REQUESTED);
        reportMapper.insert(report);

        //同时把对应帖子的状态变为0（受理中）
        LambdaUpdateWrapper<LostItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(LostItem::getId, targetId)
                .set(LostItem::getStatus, PostStatus.REQUESTED);
        lostMapper.update(wrapper);
    }

    /**
     * 物品确认认领的方法
     * @param id
     * @param userId
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void confirmItem(Long id, Long userId) {
        LostItem lostItem = checkInfoExist(id);

        if (!lostItem.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人的失物信息");
        }

        LambdaUpdateWrapper<LostItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LostItem::getId, id)
                .set(LostItem::getStatus, PostStatus.FINISHED);
        lostMapper.update(updateWrapper);
    }

    /**
     * 失物信息是否存在的统一校验
     * @param id
     * @return
     */
    @Cacheable(value = "lostItem", key = "#id")
    public LostItem checkInfoExist(Long id){
        //简单校验一下失物信息是否存在
        LambdaQueryWrapper<LostItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LostItem::getId, id);
        LostItem lostItem = lostMapper.selectOne(queryWrapper);

        if(lostItem == null){
            throw new BusinessException("失物信息不存在");
        }
        return lostItem;
    }

}
