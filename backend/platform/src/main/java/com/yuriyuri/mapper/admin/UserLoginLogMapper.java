package com.yuriyuri.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuriyuri.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
    @Select("select count(distinct user_id) from user_login_log where login_time between #{start} and #{end}")
    long countDistinctUserId(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
