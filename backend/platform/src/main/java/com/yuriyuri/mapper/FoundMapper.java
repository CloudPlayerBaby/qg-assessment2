package com.yuriyuri.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuriyuri.entity.FoundItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FoundMapper extends BaseMapper<FoundItem> {
}
