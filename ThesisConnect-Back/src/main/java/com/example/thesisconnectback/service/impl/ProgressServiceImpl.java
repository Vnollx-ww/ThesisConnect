package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Progress;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.mapper.ProgressMapper;
import com.example.thesisconnectback.service.ProgressService;
import com.example.thesisconnectback.service.SelectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 进度记录服务实现类
 */
@Slf4j
@Service
public class ProgressServiceImpl extends ServiceImpl<ProgressMapper, Progress> implements ProgressService {

    @Autowired
    private SelectionService selectionService;

    @Override
    public boolean updateProgress(Long selectionId, String milestoneTitle, String milestoneStatus, Integer percentage, String description, String problems, Long studentId, String studentName) {
        try {
            // 首先获取选题记录，从中获取topicId
            Selection selection = selectionService.getById(selectionId);
            if (selection == null) {
                log.error("选题记录不存在，selectionId: {}", selectionId);
                return false;
            }

            Progress progress = new Progress();
            progress.setSelectionId(selectionId);
            progress.setStudentId(studentId);
            progress.setStudentName(studentName);
            progress.setTopicId(selection.getTopicId()); // 设置课题ID
            progress.setPercentage(percentage);
            progress.setDescription(description);
            progress.setProblems(problems);
            
            // 设置里程碑信息
            progress.setMilestoneTitle(milestoneTitle != null && !milestoneTitle.trim().isEmpty() ? milestoneTitle : "进度更新");
            progress.setMilestoneDescription(description);
            // 状态管理：优先使用用户选择的状态，如果没有选择则根据进度智能判断
            String finalStatus = milestoneStatus != null && !milestoneStatus.trim().isEmpty() 
                ? milestoneStatus 
                : (percentage >= 100 ? "completed" : "current");
            progress.setMilestoneStatus(finalStatus);
            progress.setMilestoneDate(LocalDateTime.now());
            
            progress.setCreateTime(LocalDateTime.now());
            progress.setUpdateTime(LocalDateTime.now());

            // 保存进度记录
            boolean progressSaved = this.save(progress);
            
            if (progressSaved) {
                // 同时更新选题记录的进度字段
                selection.setProgress(percentage);
                selection.setProgressDescription(description);
                selection.setProblems(problems);
                selection.setUpdateTime(LocalDateTime.now());
                
                boolean selectionUpdated = selectionService.updateById(selection);
                if (!selectionUpdated) {
                    log.warn("进度记录保存成功，但选题记录更新失败，selectionId: {}", selectionId);
                }
            }
            
            return progressSaved;
        } catch (Exception e) {
            log.error("更新进度失败：", e);
            return false;
        }
    }

    @Override
    public boolean addMilestone(Long selectionId, String title, String description, String status, Long studentId, String studentName) {
        try {
            // 首先获取选题记录，从中获取topicId
            Selection selection = selectionService.getById(selectionId);
            if (selection == null) {
                log.error("选题记录不存在，selectionId: {}", selectionId);
                return false;
            }

            Progress progress = new Progress();
            progress.setSelectionId(selectionId);
            progress.setStudentId(studentId);
            progress.setStudentName(studentName);
            progress.setTopicId(selection.getTopicId()); // 设置课题ID
            progress.setMilestoneTitle(title);
            progress.setMilestoneDescription(description);
            progress.setMilestoneStatus(status);
            progress.setMilestoneDate(LocalDateTime.now());
            progress.setCreateTime(LocalDateTime.now());
            progress.setUpdateTime(LocalDateTime.now());

            return this.save(progress);
        } catch (Exception e) {
            log.error("添加里程碑失败：", e);
            return false;
        }
    }

    @Override
    public List<Progress> getProgressBySelectionId(Long selectionId) {
        return baseMapper.findBySelectionId(selectionId);
    }

    @Override
    public List<Progress> getProgressByStudentId(Long studentId) {
        return baseMapper.findByStudentId(studentId);
    }

    @Override
    public List<Progress> getProgressByTopicId(Long topicId) {
        return baseMapper.findByTopicId(topicId);
    }

    @Override
    public Progress getLatestProgress(Long selectionId) {
        return baseMapper.getLatestProgress(selectionId);
    }

    @Override
    public boolean updateMilestoneStatus(Long milestoneId, String milestoneStatus) {
        try {
            Progress progress = this.getById(milestoneId);
            if (progress == null) {
                log.error("里程碑记录不存在，milestoneId: {}", milestoneId);
                return false;
            }
            
            progress.setMilestoneStatus(milestoneStatus);
            progress.setUpdateTime(LocalDateTime.now());
            
            return this.updateById(progress);
        } catch (Exception e) {
            log.error("更新里程碑状态失败：", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getProgressStats() {
        return baseMapper.getProgressStats();
    }
}
