package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统键值配置（选题时间窗、最大志愿数等）
 */
@Data
@TableName("sys_system_config")
public class SystemConfig {

    @TableId(value = "config_key", type = IdType.INPUT)
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
