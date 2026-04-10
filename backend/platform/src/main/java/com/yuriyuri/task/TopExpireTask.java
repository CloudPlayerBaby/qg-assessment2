package com.yuriyuri.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TopExpireTask {
    @Autowired
    private LostMapper lostMapper;
    @Autowired
    private FoundMapper foundMapper;

    //日志记录置顶清除
    private static final Logger log = LoggerFactory.getLogger(TopExpireTask.class);
    /**
     * 每小时执行一次，将已过期的置顶记录改为普通状态
     */
    @Scheduled(cron = "0 0 * * * *")  // 每小时整点执行
    public void expireTop() {
        // 更新条件：top_end_time 不为空且小于当前时间
        LambdaUpdateWrapper<LostItem> lostWrapper = new LambdaUpdateWrapper<>();
        lostWrapper.lt(LostItem::getTopEndTime, LocalDateTime.now())
                .set(LostItem::getSortOrder, 0)
                .set(LostItem::getApplyTop, 0)
                .set(LostItem::getTopEndTime, null);
        int lostRows = lostMapper.update(null, lostWrapper);

        if (lostRows > 0) {
            log.info("已清除 {} 条过期的失物置顶记录", lostRows);
        }

        LambdaUpdateWrapper<FoundItem> foundWrapper = new LambdaUpdateWrapper<>();
        foundWrapper.lt(FoundItem::getTopEndTime, LocalDateTime.now())
                .set(FoundItem::getSortOrder, 0)
                .set(FoundItem::getApplyTop, 0)
                .set(FoundItem::getTopEndTime, null);
        int foundRows = foundMapper.update(null, foundWrapper);

        if (foundRows > 0) {
            log.info("已清除 {} 条过期的拾物置顶记录", foundRows);
        }
    }
}