package com.yuriyuri.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@TableName("private_message")
public class PrivateMessage {
    @TableId
    private Long id;

    /** 与库字段 session_id 对应，须为 VARCHAR（见 sql/fix_private_message_session_id.sql） */
    @TableField(value = "session_id", jdbcType = JdbcType.VARCHAR)
    private String sessionId;

    private Long senderId;

    private Long receiverId;

    private String content;

    /** 0 未读 1 已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
