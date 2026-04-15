-- Phase1: 站内通知表（若尚无）+ 选题规则扩展键
USE thesis_connect;

CREATE TABLE IF NOT EXISTS sys_site_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户',
    biz_type VARCHAR(64) NOT NULL COMMENT '业务类型',
    title VARCHAR(200) NOT NULL,
    content TEXT,
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    related_type VARCHAR(32) COMMENT '关联实体',
    related_id BIGINT COMMENT '关联ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_site_notif_user_read (user_id, read_flag),
    INDEX idx_site_notif_user_time (user_id, create_time DESC),
    CONSTRAINT fk_site_notif_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知';

INSERT IGNORE INTO sys_system_config (config_key, config_value) VALUES
('allow_cross_major', 'true'),
('max_selections_per_teacher_per_student', '99');
