package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Progress;
import com.example.thesisconnectback.mapper.ProgressMapper;
import com.example.thesisconnectback.service.ProgressService;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public boolean updateProgress(Long selectionId, Integer percentage, String description, String problems, Long studentId, String studentName) {
        try {
            Progress progress = new Progress();
            progress.setSelectionId(selectionId);
            progress.setStudentId(studentId);
            progress.setStudentName(studentName);
            progress.setPercentage(percentage);
            progress.setDescription(description);
            progress.setProblems(problems);
            progress.setCreateTime(LocalDateTime.now());
            progress.setUpdateTime(LocalDateTime.now());

            return this.save(progress);
        } catch (Exception e) {
            log.error("更新进度失败：", e);
            return false;
        }
    }

    @Override
    public boolean addMilestone(Long selectionId, String title, String description, String status, Long studentId, String studentName) {
        try {
            Progress progress = new Progress();
            progress.setSelectionId(selectionId);
            progress.setStudentId(studentId);
            progress.setStudentName(studentName);
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
    public Map<String, Object> getProgressStats() {
        return baseMapper.getProgressStats();
    }
}
