package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 选题记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_selection")
public class Selection {

    /**
     * 选题ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 学生学号
     */
    @TableField("student_number")
    private String studentNumber;

    /**
     * 课题ID
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * 课题标题
     */
    @TableField("topic_title")
    private String topicTitle;

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
     * 选题时间
     */
    @TableField("selection_time")
    private LocalDateTime selectionTime;

    /**
     * 状态：pending-待审核，approved-审核通过（学生需确认），confirmed-已确认（选题成功），rejected-已拒绝，active-进行中，completed-已完成
     */
    @TableField("status")
    private String status;

    /**
     * 进度百分比
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 进度描述
     */
    @TableField("progress_description")
    private String progressDescription;

    /**
     * 遇到的问题
     */
    @TableField("problems")
    private String problems;

    /**
     * 最终成绩
     */
    @TableField("final_grade")
    private String finalGrade;

    /**
     * 教师评价
     */
    @TableField("teacher_evaluation")
    private String teacherEvaluation;

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
