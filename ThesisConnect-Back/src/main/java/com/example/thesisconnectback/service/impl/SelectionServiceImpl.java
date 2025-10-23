package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.mapper.SelectionMapper;
import com.example.thesisconnectback.mapper.TopicMapper;
import com.example.thesisconnectback.mapper.UserMapper;
import com.example.thesisconnectback.service.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选题记录服务实现类
 */
@Service
public class SelectionServiceImpl extends ServiceImpl<SelectionMapper, Selection> implements SelectionService {

    @Autowired
    private SelectionMapper selectionMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Selection> findByStudentId(Long studentId) {
        return selectionMapper.findByStudentId(studentId);
    }

    @Override
    public List<Selection> findByTopicId(Long topicId) {
        return selectionMapper.findByTopicId(topicId);
    }

    @Override
    public List<Selection> findByTeacherId(Long teacherId) {
        return selectionMapper.findByTeacherId(teacherId);
    }

    @Override
    public List<Selection> findByStatus(String status) {
        return selectionMapper.findByStatus(status);
    }

    @Override
    @Transactional
    public boolean selectTopic(Long studentId, Long topicId) {
        // 检查学生是否已选择课题
        if (hasSelectedTopic(studentId)) {
            return false;
        }

        // 检查课题是否已满员
        if (isTopicFull(topicId)) {
            return false;
        }

        // 获取课题信息
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            return false;
        }

        // 获取学生信息
        User student = userMapper.selectById(studentId);
        if (student == null) {
            return false;
        }

        // 创建选题记录
        Selection selection = new Selection();
        selection.setStudentId(studentId);
        selection.setStudentName(student.getRealName());
        selection.setStudentNumber(student.getStudentId());
        selection.setTopicId(topicId);
        selection.setTopicTitle(topic.getTitle());
        selection.setTeacherId(topic.getTeacherId());
        selection.setTeacherName(topic.getTeacherName());
        selection.setSelectionTime(LocalDateTime.now());
        selection.setStatus("pending");
        selection.setProgress(0);
        selection.setCreateTime(LocalDateTime.now());
        selection.setUpdateTime(LocalDateTime.now());

        return save(selection);
    }

    @Override
    @Transactional
    public boolean cancelSelection(Long selectionId) {
        Selection selection = getById(selectionId);
        if (selection != null && "pending".equals(selection.getStatus())) {
            // 更新课题已选学生数
            Topic topic = topicMapper.selectById(selection.getTopicId());
            if (topic != null) {
                topicMapper.updateSelectedCount(topic.getId(), topic.getSelectedCount() - 1);
            }
            return removeById(selectionId);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean reviewSelection(Long selectionId, String status, String comment) {
        Selection selection = getById(selectionId);
        if (selection != null) {
            selection.setStatus(status);
            selection.setUpdateTime(LocalDateTime.now());
            
            if ("approved".equals(status)) {
                // 更新课题已选学生数
                Topic topic = topicMapper.selectById(selection.getTopicId());
                if (topic != null) {
                    topicMapper.updateSelectedCount(topic.getId(), topic.getSelectedCount() + 1);
                }
            }
            
            return updateById(selection);
        }
        return false;
    }

    @Override
    public boolean updateProgress(Long selectionId, Integer progress, String description, String problems) {
        Selection selection = getById(selectionId);
        if (selection != null) {
            selection.setProgress(progress);
            selection.setProgressDescription(description);
            selection.setProblems(problems);
            selection.setUpdateTime(LocalDateTime.now());
            return updateById(selection);
        }
        return false;
    }

    @Override
    public boolean hasSelectedTopic(Long studentId) {
        return selectionMapper.countByStudentId(studentId) > 0;
    }

    @Override
    public boolean isTopicFull(Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic != null) {
            int approvedCount = selectionMapper.countApprovedByTopicId(topicId);
            return approvedCount >= topic.getMaxStudents();
        }
        return true;
    }

    @Override
    public Map<String, Object> getSelectionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总选题数
        long totalSelections = count();
        stats.put("totalSelections", totalSelections);
        
        // 各状态选题数
        List<Selection> pendingSelections = findByStatus("pending");
        List<Selection> approvedSelections = findByStatus("approved");
        List<Selection> rejectedSelections = findByStatus("rejected");
        
        stats.put("pendingSelections", pendingSelections.size());
        stats.put("approvedSelections", approvedSelections.size());
        stats.put("rejectedSelections", rejectedSelections.size());
        
        return stats;
    }
}
