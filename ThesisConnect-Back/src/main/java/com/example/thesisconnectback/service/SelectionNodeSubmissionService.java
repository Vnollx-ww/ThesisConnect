package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.SelectionNodeSubmission;

public interface SelectionNodeSubmissionService extends IService<SelectionNodeSubmission> {

    SelectionNodeSubmission findLatest(Long selectionId, int nodeIndex);

    /** 当前节点最新一条是否已审核通过 */
    boolean isLatestApprovedForNode(Long selectionId, int nodeIndex);

    void submitMaterial(Long selectionId, Long studentId, String description, String reportUrl);

    void reviewMaterial(Long selectionId, Long submissionId, Long reviewerUserId, String reviewerRole, String status, String rejectReason);
}
