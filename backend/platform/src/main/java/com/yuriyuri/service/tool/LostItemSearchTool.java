package com.yuriyuri.service.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuriyuri.entity.LostItem;
import com.yuriyuri.mapper.LostMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LostItemSearchTool {
    @Autowired
    private LostMapper lostMapper;

    /**
     * 如果用户想发布拾物，则代表要去失物表找匹配
     * @return
     */
    @Tool(description = "查询所有未被认领的失物记录，返回表供AI分析匹配")
    public List<LostItem> getAllLostItem() {
        LambdaQueryWrapper<LostItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostItem::getStatus,1);
        return lostMapper.selectList(wrapper);
    }

}
