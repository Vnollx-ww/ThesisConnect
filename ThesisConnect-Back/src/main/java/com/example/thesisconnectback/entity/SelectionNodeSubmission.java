package com.example.thesisconnectback.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 进度链路某节点学生提交的材料（教师审核通过后方可推进该阶段）
 */
@Data
@TableName("sys_selection_node_submission")
public class SelectionNodeSubmission {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("selection_id")
    private Long selectionId;

    /** 链路节点序号，与 buildChainView 中 nodes 下标一致 */
    @TableField("node_index")
    private Integer nodeIndex;

    @TableField("description")
    private String description;

    @TableField("report_url")
    private String reportUrl;

    @TableField("status")
    private String status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("reviewer_name")
    private String reviewerName;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
