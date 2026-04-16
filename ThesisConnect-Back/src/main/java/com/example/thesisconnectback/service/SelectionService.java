package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 选题记录服务接口
 */
public interface SelectionService extends IService<Selection> {

    /**
     * 根据学生ID查询选题记录
     */
    List<Selection> findByStudentId(Long studentId);

    /**
     * 根据课题ID查询选题记录
     */
    List<Selection> findByTopicId(Long topicId);

    /**
     * 根据教师ID查询选题记录
     */
    List<Selection> findByTeacherId(Long teacherId);

    /**
     * 根据状态查询选题记录
     */
    List<Selection> findByStatus(String status);

    /**
     * 校验选题请求（开放时间与志愿数等），不通过时抛出 BusinessException
     */
    void validateSelectTopicRequest(Long studentId, Long topicId);

    /**
     * 学生选择课题；失败时抛出 {@link com.example.thesisconnectback.exception.BusinessException}
     */
    Selection selectTopic(Long studentId, Long topicId);

    /**
     * 取消选择课题
     */
    boolean cancelSelection(Long selectionId);

    /**
     * 审核选题
     */
    boolean reviewSelection(Long selectionId, String status, String comment);

    /**
     * 批量审核（教师/管理员）；返回 successIds 与 failures（id + message）
     */
    Map<String, Object> batchReviewSelections(Long reviewerUserId, String reviewerRole, List<Long> selectionIds, String status, String comment);

    /**
     * 更新选题状态
     */
    boolean updateSelectionStatus(Long selectionId, String status);

    /**
     * 更新进度
     */
    boolean updateProgress(Long selectionId, Integer progress, String description, String problems);

    /**
     * 评价打分
     */
    boolean gradeSelection(Long selectionId, String grade, String evaluation);

    /**
     * 检查学生是否已选择课题
     */
    boolean hasSelectedTopic(Long studentId);

    /**
     * 检查课题是否已满员
     */
    boolean isTopicFull(Long topicId);

    /**
     * 获取选题统计信息
     */
    Map<String, Object> getSelectionStats();

    /**
     * 根据课题ID获取学生列表
     */
    List<User> getStudentsByTopicId(Long topicId);

    /**
     * Get recent selections
     */
    List<Map<String, Object>> getRecentSelections(int limit);

    /**
     * 教师/管理员调整选题在预定进度链上的完成节点数。action: next | prev | set
     */
    boolean adjustProgressChainStep(Long selectionId, String action, Integer setCount, Long operatorUserId, String operatorRole);

    /**
     * 学生/教师/管理员查看预定进度链路展示（含节点状态）
     */
    Map<String, Object> getProgressChainView(Long selectionId, Long requestUserId, String role);
}
