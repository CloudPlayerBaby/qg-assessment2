package com.yuriyuri.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuriyuri.common.BusinessException;
import com.yuriyuri.dto.admin.ReportRequest;
import com.yuriyuri.dto.admin.TopRequest;
import com.yuriyuri.dto.found.FoundInfoRequest;
import com.yuriyuri.dto.lost.LostInfoRequest;
import com.yuriyuri.entity.*;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import com.yuriyuri.mapper.ReportMapper;
import com.yuriyuri.mapper.UserMapper;
import com.yuriyuri.mapper.admin.UserLoginLogMapper;
import com.yuriyuri.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LostMapper lostMapper;
    @Autowired
    private FoundMapper foundMapper;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    //以下为置顶功能

    /**
     * 管理员查看信息
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<LostItem> getLostInfo(LostInfoRequest req, int pageNum, int pageSize) {
        Page<LostItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostItem::getApplyTop, 1)
                .like(req.getItemName() != null, LostItem::getItemName, req.getItemName())
                .like(req.getLostPlace() != null, LostItem::getLostPlace, req.getLostPlace())
                .orderByDesc(LostItem::getApplyTop)
                .orderByDesc(LostItem::getUpdateTime);
        Page<LostItem> lostItemPage = lostMapper.selectPage(page, wrapper);

        if (lostItemPage.getTotal() <= 0) {
            return null;
        }
        return lostItemPage;
    }

    @Override
    public Page<FoundItem> getFoundInfo(FoundInfoRequest req, int pageNum, int pageSize) {
        Page<FoundItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoundItem::getApplyTop, 1)
                .like(req.getItemName() != null, FoundItem::getItemName, req.getItemName())
                .like(req.getFoundPlace() != null, FoundItem::getFoundPlace, req.getFoundPlace())
                .orderByDesc(FoundItem::getApplyTop)
                .orderByDesc(FoundItem::getUpdateTime);

        Page<FoundItem> foundItemPage = foundMapper.selectPage(page, wrapper);

        if (foundItemPage.getTotal() <= 0) {
            return null;
        }
        return foundItemPage;
    }

    /**
     * 审核通过后进行的处理，失物和拾物同理
     * applyTop含义：0-正常（不选择置顶），1-置顶审核中，2-置顶审核通过，-1-置顶审核被驳回
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void setLostTop(TopRequest req) {
        LocalDateTime topEndTime = LocalDateTime.now().plusHours(req.getHours());
        //更新sort_order为1，apply_top（接受置顶）为2，top_end_time为(now+hours)
        LambdaUpdateWrapper<LostItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(LostItem::getId, req.getId())
                .set(LostItem::getSortOrder, 1)
                .set(LostItem::getApplyTop, 2)
                .set(LostItem::getTopEndTime, topEndTime);
        lostMapper.update(wrapper);
    }

    /**
     * 拾物同理，不再写注释
     *
     * @param req
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void setFoundTop(TopRequest req) {
        LocalDateTime topEndTime = LocalDateTime.now().plusHours(req.getHours());
        LambdaUpdateWrapper<FoundItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FoundItem::getId, req.getId())
                .set(FoundItem::getSortOrder, 1)
                .set(FoundItem::getApplyTop, 2)
                .set(FoundItem::getTopEndTime, topEndTime);
        foundMapper.update(wrapper);
    }

    /**
     * 管理员拒绝置顶
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void rejectLostTop(Long id) {
        LambdaUpdateWrapper<LostItem> wrapper = new LambdaUpdateWrapper<>();
        //置顶请求-1即为被驳回
        wrapper.eq(LostItem::getId, id)
                .set(LostItem::getApplyTop, -1);
        lostMapper.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void rejectFoundTop(Long id) {
        LambdaUpdateWrapper<FoundItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FoundItem::getId, id)
                .set(FoundItem::getApplyTop, -1);
        foundMapper.update(wrapper);
    }

    //以下为帖子举报功能

    /**
     * 查看所有的帖子举报内容
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<Report> getPostReport(int pageNum, int pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        //按照创建时间查看
        wrapper.orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(page, wrapper);

        if (reportPage.getTotal() <= 0) {
            return null;
        }
        return reportPage;
    }

    /**
     * 接受举报，封贴
     *
     * @param id
     * @param req
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptPostReport(Long id, ReportRequest req) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }

        //两个update，一个是把举报请求状态改变，一个是把被举报的帖子封禁
        //把举报请求状态改变，即update report set status=0 where id=?
        LambdaUpdateWrapper<Report> wrapper1 = new LambdaUpdateWrapper<>();
        wrapper1.eq(Report::getId, id)
                .set(Report::getStatus, 0);
        reportMapper.update(wrapper1);

        //把举报的帖子封禁
        updatePostStatus(report.getTargetId(), report.getType(), -1);
    }

    /**
     * 拒绝举报，改回状态
     *
     * @param id
     * @param req
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void rejectPostReport(Long id, ReportRequest req) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }

        //也是两个update，一个是把举报请求状态改变，一个是把被举报的帖子状态改为1（正常）
        //把举报请求状态改变，即update report set status=-1 where id=?
        LambdaUpdateWrapper<Report> wrapper1 = new LambdaUpdateWrapper<>();
        wrapper1.eq(Report::getId, id)
                .set(Report::getStatus, -1);
        reportMapper.update(wrapper1);

        //帖子状态变回1
        updatePostStatus(report.getTargetId(), report.getType(), 1);
    }

    /**
     * 查看所有用户
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<User> selectAllUsers(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        //以创建时间倒叙查看
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 封禁用户
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long id) {
        //0就是封了
        updateUserStatus(id,0);
    }

    /**
     * 解封用户
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unBanUser(Long id) {
        //1就是解封
        updateUserStatus(id,1);
    }

    /**
     * 封禁帖子
     * @param id
     * @param type
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banPost(Long id, String type) {
        updatePostStatus(id, type, -1);
    }

    /**
     * 解封帖子
     * @param id
     * @param type
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unBanPost(Long id, String type) {
        updatePostStatus(id, type, 1);
    }

    /**
     * 删除帖子
     * @param id
     * @param type
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id, String type) {
        if("lost".equals(type)){
            if(lostMapper.selectById(id)==null){
                throw new BusinessException("失物贴不存在");
            }
            lostMapper.deleteById(id);
        }
        if("found".equals(type)){
            if(foundMapper.selectById(id)==null){
                throw new BusinessException("拾物贴不存在");
            }
            foundMapper.deleteById(id);
        }
    }

    /**
     * 获取发布信息数量
     * @return
     */
    @Override
    public Long getPostNumber() {
        LambdaQueryWrapper<LostItem> lostWrapper = new LambdaQueryWrapper<>();
        Long lostPostNumber = lostMapper.selectCount(lostWrapper);
        LambdaQueryWrapper<FoundItem> foundWrapper = new LambdaQueryWrapper<>();
        Long foundPostNumber = foundMapper.selectCount(foundWrapper);
        return lostPostNumber+foundPostNumber;
    }

    /**
     * 获取找回物品数量
     * @return
     */
    @Override
    public Long getCompletedPostNumber() {
        //状态为2的为已完成（已找回）
        LambdaQueryWrapper<LostItem> lostWrapper = new LambdaQueryWrapper<>();
        lostWrapper.eq(LostItem::getStatus, 2);
        Long lostPostNumber = lostMapper.selectCount(lostWrapper);
        LambdaQueryWrapper<FoundItem> foundWrapper = new LambdaQueryWrapper<>();
        foundWrapper.eq(FoundItem::getStatus,2);
        Long foundPostNumber = foundMapper.selectCount(foundWrapper);
        return lostPostNumber+foundPostNumber;
    }

    /**
     * 获取活跃用户数（24h内）
     * @return
     */
    @Override
    public Long getActiveUsersNumber(LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        return userLoginLogMapper.countDistinctUserId(startTime, endTime);
    }

    /**
     * 获取7天内失物地点统计
     */
    @Override
    public List<Map<String, Object>> getLostPlaceStatistics(LocalDateTime startTime) {
        //从数据库获取七天内的数据，不包括被封禁的
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(LostItem::getCreateTime, startTime)
                .ne(LostItem::getStatus, -1);
        List<LostItem> items = lostMapper.selectList(wrapper);

        //key为lostPlace，value为对应lostPlace的count
        Map<String, Long> countMap = new HashMap<>();
        //遍历该map
        for (LostItem item : items) {
            String place = item.getLostPlace();
            if (countMap.containsKey(place)) {
                //如果包含则+1
                countMap.put(place, countMap.get(place) + 1);
            } else {
                //不包含则创建一个1
                countMap.put(place, 1L);
            }
        }
        
        return buildStatistics(countMap, "location");
    }

    /**
     * 获取7天内失物物品统计
     */
    @Override
    public List<Map<String, Object>> getLostItemStatistics(LocalDateTime startTime) {
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(LostItem::getCreateTime, startTime)
                .ne(LostItem::getStatus, -1);
        List<LostItem> items = lostMapper.selectList(wrapper);
        
        Map<String, Long> countMap = new HashMap<>();
        for (LostItem item : items) {
            String itemName = item.getItemName();
            if (countMap.containsKey(itemName)) {
                countMap.put(itemName, countMap.get(itemName) + 1);
            } else {
                countMap.put(itemName, 1L);
            }
        }
        
        return buildStatistics(countMap, "item");
    }

    /**
     * 获取7天内拾物地点统计
     */
    @Override
    public List<Map<String, Object>> getFoundPlaceStatistics(LocalDateTime startTime) {
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FoundItem::getCreateTime, startTime)
                .ne(FoundItem::getStatus, -1);
        List<FoundItem> items = foundMapper.selectList(wrapper);
        
        Map<String, Long> countMap = new HashMap<>();
        for (FoundItem item : items) {
            String place = item.getFoundPlace();
            if (countMap.containsKey(place)) {
                countMap.put(place, countMap.get(place) + 1);
            } else {
                countMap.put(place, 1L);
            }
        }
        
        return buildStatistics(countMap, "location");
    }

    /**
     * 获取7天内拾物物品统计
     */
    @Override
    public List<Map<String, Object>> getFoundItemStatistics(LocalDateTime startTime) {
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(FoundItem::getCreateTime, startTime)
                .ne(FoundItem::getStatus, -1);
        List<FoundItem> items = foundMapper.selectList(wrapper);
        
        Map<String, Long> countMap = new HashMap<>();
        for (FoundItem item : items) {
            String itemName = item.getItemName();
            if (countMap.containsKey(itemName)) {
                countMap.put(itemName, countMap.get(itemName) + 1);
            } else {
                countMap.put(itemName, 1L);
            }
        }
        
        return buildStatistics(countMap, "item");
    }

    //以下为方法

    //改变用户的状态，可以是封禁也可以是解禁
    public void updateUserStatus(Long id,int status) {
        User user = userMapper.selectById(id);
        //基础校验，用户存在才能封
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id)
                .set(User::getStatus, status);
        userMapper.update(wrapper);
    }

    //改变帖子状态的方法，可以是封禁也可以是不封禁、解禁
    public void updatePostStatus(Long id, String type, int status) {
        //如果是失物贴
        if ("lost".equals(type)) {
            //校验一下
            if (lostMapper.selectById(id) == null) {
                throw new BusinessException("失物帖不存在");
            }
            LambdaUpdateWrapper<LostItem> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2.eq(LostItem::getId, id)
                    .set(LostItem::getStatus, status);
            lostMapper.update(wrapper2);
        }
        //如果是拾物贴
        if ("found".equals(type)) {
            if (foundMapper.selectById(id) == null) {
                throw new BusinessException("拾物帖不存在");
            }
            LambdaUpdateWrapper<FoundItem> wrapper2 = new LambdaUpdateWrapper<>();
            wrapper2.eq(FoundItem::getId, id)
                    .set(FoundItem::getStatus, status);
            foundMapper.update(wrapper2);
        }
    }

    //返回一个List，这个List包含着多个Map
    private List<Map<String, Object>> buildStatistics(Map<String, Long> countMap, String keyName) {
        List<Map<String, Object>> result = new ArrayList<>();

        //遍历Map集合
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            //keyName即为形参传来的，用于分类的名字
            map.put(keyName, entry.getKey());
            //count即为在上面重写方法里查询到的数量
            map.put("count", entry.getValue());
            //每一个Map都是类似{"location": "图书馆", "count": 156}的格式
            result.add(map);
        }

        //最后的result大概长这样：[{"location": "图书馆", "count": 156}, {"location": "第一食堂", "count": 134}]
        return result;
    }
}
