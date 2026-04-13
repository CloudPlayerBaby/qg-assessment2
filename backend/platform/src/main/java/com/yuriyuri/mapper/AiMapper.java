package com.yuriyuri.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuriyuri.entity.AiSuggestion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMapper extends BaseMapper<AiSuggestion> {
}
