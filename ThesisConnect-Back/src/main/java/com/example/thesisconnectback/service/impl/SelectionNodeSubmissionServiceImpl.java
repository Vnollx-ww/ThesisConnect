package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.SelectionNodeSubmission;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.exception.BusinessException;
import com.example.thesisconnectback.mapper.SelectionNodeSubmissionMapper;
import com.example.thesisconnectback.mapper.UserMapper;
import com.example.thesisconnectback.service.ProgressChainService;
import com.example.thesisconnectback.service.SelectionNodeSubmissionService;
import com.example.thesisconnectback.service.SelectionService;
import com.example.thesisconnectback.service.SiteNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SelectionNodeSubmissionServiceImpl extends ServiceImpl<SelectionNodeSubmissionMapper, SelectionNodeSubmission>
        implements SelectionNodeSubmissionService {

    @Autowired
    private SelectionService selectionService;

    @Autowired
    private ProgressChainService progressChainService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SiteNotificationService siteNotificationService;

    @Override
    public SelectionNodeSubmission findLatest(Long selectionId, int nodeIndex) {
        return getOne(new QueryWrapper<SelectionNodeSubmission>()
                .eq("selection_id", selectionId)
                .eq("node_index", nodeIndex)
                .orderByDesc("id")
                .last("LIMIT 1"));
    }

    @Override
    public boolean isLatestApprovedForNode(Long selectionId, int nodeIndex) {
        SelectionNodeSubmission latest = findLatest(selectionId, nodeIndex);
        return latest != null && "approved".equals(latest.getStatus());
    }

    @Override
    @Transactional
    public void submitMaterial(Long selectionId, Long studentId, String description, String reportUrl) {
        String d = description != null ? description.trim() : "";
        String u = reportUrl != null ? reportUrl.trim() : "";
        if (d.isEmpty() && u.isEmpty()) {
            throw new BusinessException(400, "请填写阶段说明或材料链接");
        }
        if (u.length() > 500) {
            throw new BusinessException(400, "材料链接长度不能超过 500 字符");
        }

        Selection selection = selectionService.getById(selectionId);
        if (selection == null) {
            throw new BusinessException(400, "选题记录不存在");
        }
        if (studentId == null || !studentId.equals(selection.getStudentId())) {
            throw new BusinessException(403, "只能提交本人的选题材料");
        }
        if (!"confirmed".equals(selection.getStatus()) && !"active".equals(selection.getStatus())) {
            throw new BusinessException(400, "当前选题状态不可提交阶段材料");
        }
        if (selection.getProgressChainId() == null) {
            throw new BusinessException(400, "尚未套用进度链路，请联系管理员");
        }

        int n = progressChainService.countNodes(selection.getProgressChainId());
        if (n <= 0) {
            throw new BusinessException(400, "进度链路未配置节点");
        }

        int c = selection.getProgressCompletedCount() != null ? selection.getProgressCompletedCount() : 0;
        if (c >= n) {
            throw new BusinessException(400, "当前进度已全部完成，无需再提交");
        }

        SelectionNodeSubmission latest = findLatest(selectionId, c);
        if (latest != null) {
            if ("pending".equals(latest.getStatus())) {
                throw new BusinessException(400, "已有待审核的提交，请等待指导教师审核");
            }
            if ("approved".equals(latest.getStatus())) {
                throw new BusinessException(400, "本阶段材料已通过，请等待教师推进到下一阶段后再提交");
            }
        }

        SelectionNodeSubmission row = new SelectionNodeSubmission();
        row.setSelectionId(selectionId);
        row.setNodeIndex(c);
        row.setDescription(d.isEmpty() ? null : d);
        row.setReportUrl(u.isEmpty() ? null : u);
        row.setStatus("pending");
        save(row);
    }

    @Override
    @Transactional
    public void reviewMaterial(Long selectionId, Long submissionId, Long reviewerUserId, String reviewerRole, String status, String rejectReason) {
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new BusinessException(400, "审核状态无效");
        }
        if ("rejected".equals(status) && (rejectReason == null || rejectReason.trim().isEmpty())) {
            throw new BusinessException(400, "驳回时请填写原因");
        }

        SelectionNodeSubmission sub = getById(submissionId);
        if (sub == null) {
            throw new BusinessException(400, "提交记录不存在");
        }
        if (selectionId == null || !selectionId.equals(sub.getSelectionId())) {
            throw new BusinessException(400, "记录与选题不匹配");
        }
        if (!"pending".equals(sub.getStatus())) {
            throw new BusinessException(400, "该条记录已审核，请勿重复操作");
        }

        Selection selection = selectionService.getById(sub.getSelectionId());
        if (selection == null) {
            throw new BusinessException(400, "选题记录不存在");
        }

        if ("teacher".equals(reviewerRole)) {
            if (reviewerUserId == null || !reviewerUserId.equals(selection.getTeacherId())) {
                throw new BusinessException(403, "只能审核本人指导的选题");
            }
        } else if (!"admin".equals(reviewerRole)) {
            throw new BusinessException(403, "权限不足");
        }

        User reviewer = reviewerUserId != null ? userMapper.selectById(reviewerUserId) : null;
        String reviewerName = reviewer != null && reviewer.getRealName() != null ? reviewer.getRealName() : "";

        sub.setStatus(status);
        sub.setRejectReason("rejected".equals(status) ? rejectReason.trim() : null);
        sub.setReviewerId(reviewerUserId);
        sub.setReviewerName(reviewerName);
        sub.setReviewTime(LocalDateTime.now());
        updateById(sub);

        String title = "approved".equals(status) ? "阶段材料已通过" : "阶段材料需修改";
        String content = "approved".equals(status)
                ? "您的阶段材料已通过审核，指导教师将据此推进进度。"
                : ("审核意见：" + rejectReason.trim());
        try {
            siteNotificationService.notifyUser(selection.getStudentId(), "PROGRESS_NODE_REVIEWED", title, content,
                    "selection_node_submission", sub.getId());
        } catch (Exception ignored) {
            // 通知失败不影响审核结果
        }
    }
}
