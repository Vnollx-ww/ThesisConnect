package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 站内通知（与邮件并行：重要操作双通道触达，站内可回溯）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_site_notification")
public class SiteNotification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /** 业务类型，如 TEACHER_NEW_SELECTION、STUDENT_SELECTION_APPROVED */
    @TableField("biz_type")
    private String bizType;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    /** 0 未读 1 已读 */
    @TableField("read_flag")
    private Integer readFlag;

    @TableField("related_type")
    private String relatedType;

    @TableField("related_id")
    private Long relatedId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
