package com.yuriyuri.service.tool;

import com.yuriyuri.mapper.FoundMapper;
import com.yuriyuri.mapper.LostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemSearchTool {
    @Autowired
    private LostMapper lostMapper;
    @Autowired
    private FoundMapper foundMapper;
    //如果是失物
    
}
