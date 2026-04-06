package com.yuriyuri.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuriyuri.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
