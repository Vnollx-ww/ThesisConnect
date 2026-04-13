package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.Progress;

import java.util.List;
import java.util.Map;

/**
 * 进度记录服务接口
 */
public interface ProgressService extends IService<Progress> {

    /**
     * 更新进度
     */
    boolean updateProgress(Long selectionId, String milestoneTitle, String milestoneStatus, Integer percentage, String description, String problems, String reportUrl, Long studentId, String studentName);

    /**
     * 审核进度
     */
    boolean reviewProgress(Long id, String status, String rejectReason);

    /**
     * 添加里程碑
     */
    boolean addMilestone(Long selectionId, String title, String description, String status, Long studentId, String studentName);

    /**
     * 根据选题ID获取进度记录
     */
    List<Progress> getProgressBySelectionId(Long selectionId);

    /**
     * 根据学生ID获取进度记录
     */
    List<Progress> getProgressByStudentId(Long studentId);

    /**
     * 根据课题ID获取进度记录
     */
    List<Progress> getProgressByTopicId(Long topicId);

    /**
     * 获取最新进度
     */
    Progress getLatestProgress(Long selectionId);

    /**
     * 更新里程碑状态
     */
    boolean updateMilestoneStatus(Long milestoneId, String milestoneStatus);

    /**
     * 获取进度统计信息
     */
    Map<String, Object> getProgressStats();
}
