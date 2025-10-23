package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 进度记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_progress")
public class Progress {

    /**
     * 进度ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 选题ID
     */
    @TableField("selection_id")
    private Long selectionId;

    /**
     * 学生ID
     */
    @TableField("student_id")
    private Long studentId;

    /**
     * 学生姓名
     */
    @TableField("student_name")
    private String studentName;

    /**
     * 课题ID
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * 进度百分比
     */
    @TableField("percentage")
    private Integer percentage;

    /**
     * 进度描述
     */
    @TableField("description")
    private String description;

    /**
     * 遇到的问题
     */
    @TableField("problems")
    private String problems;

    /**
     * 里程碑标题
     */
    @TableField("milestone_title")
    private String milestoneTitle;

    /**
     * 里程碑描述
     */
    @TableField("milestone_description")
    private String milestoneDescription;

    /**
     * 里程碑状态：completed-已完成，current-进行中，pending-待开始
     */
    @TableField("milestone_status")
    private String milestoneStatus;

    /**
     * 里程碑日期
     */
    @TableField("milestone_date")
    private LocalDateTime milestoneDate;

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
