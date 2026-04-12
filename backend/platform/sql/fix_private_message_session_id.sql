-- 私聊会话标识为字符串（如 lost_1_1_5），若建表时 session_id 做成了数字类型会导致写入失败。
-- 在 MySQL 中执行一次即可：

ALTER TABLE private_message
    MODIFY COLUMN session_id VARCHAR(128) NOT NULL COMMENT '会话标识';
