package com.yuriyuri.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuriyuri.entity.LostItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LostMapper extends BaseMapper<LostItem> {
}
