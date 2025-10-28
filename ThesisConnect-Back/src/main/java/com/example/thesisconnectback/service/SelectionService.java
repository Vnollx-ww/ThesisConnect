package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.Selection;

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
     * 学生选择课题
     */
    boolean selectTopic(Long studentId, Long topicId);

    /**
     * 取消选择课题
     */
    boolean cancelSelection(Long selectionId);

    /**
     * 审核选题
     */
    boolean reviewSelection(Long selectionId, String status, String comment);

    /**
     * 更新选题状态
     */
    boolean updateSelectionStatus(Long selectionId, String status);

    /**
     * 更新进度
     */
    boolean updateProgress(Long selectionId, Integer progress, String description, String problems);

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
}
