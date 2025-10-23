package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课题实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_topic")
public class Topic {

    /**
     * 课题ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课题标题
     */
    @TableField("title")
    private String title;

    /**
     * 课题描述
     */
    @TableField("description")
    private String description;

    /**
     * 指导教师ID
     */
    @TableField("teacher_id")
    private Long teacherId;

    /**
     * 指导教师姓名
     */
    @TableField("teacher_name")
    private String teacherName;

    /**
     * 专业要求
     */
    @TableField("major")
    private String major;

    /**
     * 难度等级：easy-简单，medium-中等，hard-困难
     */
    @TableField("difficulty")
    private String difficulty;

    /**
     * 最大学生数
     */
    @TableField("max_students")
    private Integer maxStudents;

    /**
     * 已选学生数
     */
    @TableField("selected_count")
    private Integer selectedCount;

    /**
     * 技术要求
     */
    @TableField("requirements")
    private String requirements;

    /**
     * 预期成果
     */
    @TableField("expected_outcome")
    private String expectedOutcome;

    /**
     * 截止时间
     */
    @TableField("deadline")
    private LocalDateTime deadline;

    /**
     * 状态：active-进行中，completed-已完成，paused-已暂停
     */
    @TableField("status")
    private String status;

    /**
     * 浏览量
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 评分
     */
    @TableField("rating")
    private Double rating;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
