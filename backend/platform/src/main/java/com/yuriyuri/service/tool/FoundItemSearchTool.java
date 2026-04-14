package com.yuriyuri.service.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuriyuri.entity.FoundItem;
import com.yuriyuri.mapper.FoundMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FoundItemSearchTool {
    @Autowired
    private FoundMapper foundMapper;
    //如果是失物

    /**
     * 如果用户想发布失物，则代表要去拾物表找匹配
     * @return
     */
    @Tool(description = "查询所有未被认领的拾物记录，返回表供AI分析匹配")
    public List<FoundItem> getAllFoundItem() {
        LambdaQueryWrapper<FoundItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoundItem::getStatus,1);
        return foundMapper.selectList(wrapper);
    }

}
