package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

        // 检查课题是否已满员（只检查已审核通过的数量）
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

        boolean saved = save(selection);
        
        // 如果保存成功，更新课题的已选学生数为待审核+已审核的数量
        if (saved) {
            // 计算该课题的选题总数（包括待审核和已审核的）
            int totalSelections = selectionMapper.countByTopicId(topicId);
            topicMapper.updateSelectedCount(topicId, totalSelections);
        }
        
        return saved;
    }

    @Override
    @Transactional
    public boolean cancelSelection(Long selectionId) {
        Selection selection = getById(selectionId);
        if (selection != null && "pending".equals(selection.getStatus())) {
            Long topicId = selection.getTopicId();
            boolean removed = removeById(selectionId);
            
            // 如果删除成功，更新课题的已选学生数
            if (removed) {
                int totalSelections = selectionMapper.countByTopicId(topicId);
                topicMapper.updateSelectedCount(topicId, totalSelections);
            }
            
            return removed;
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
            
            boolean updated = updateById(selection);
            
            // 如果更新成功，更新课题的已选学生数（因为状态改变可能影响显示）
            if (updated) {
                Long topicId = selection.getTopicId();
                int totalSelections = selectionMapper.countByTopicId(topicId);
                topicMapper.updateSelectedCount(topicId, totalSelections);
            }
            
            return updated;
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
        List<Selection> activeSelections = findByStatus("active");
        List<Selection> completedSelections = findByStatus("completed");
        
        stats.put("pendingSelections", pendingSelections.size());
        stats.put("approvedSelections", approvedSelections.size());
        stats.put("rejectedSelections", rejectedSelections.size());
        stats.put("activeSelections", activeSelections.size());
        stats.put("completedSelections", completedSelections.size());
        
        // 计算选题增长率（本月新增选题数 / 上月新增选题数）
        long currentMonthSelections = getCurrentMonthSelectionCount();
        long lastMonthSelections = getLastMonthSelectionCount();
        double selectionGrowth = lastMonthSelections > 0 ? ((double)(currentMonthSelections - lastMonthSelections) / lastMonthSelections) * 100 : 0;
        stats.put("selectionGrowth", Math.round(selectionGrowth * 100) / 100.0);
        
        // 计算完成率（已完成选题数 / 总选题数）
        double completionRate = totalSelections > 0 ? ((double)completedSelections.size() / totalSelections) * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 100) / 100.0);
        
        // 计算完成率增长率（本月完成率 - 上月完成率）
        double currentMonthCompletionRate = getCurrentMonthCompletionRate();
        double lastMonthCompletionRate = getLastMonthCompletionRate();
        double completionGrowth = currentMonthCompletionRate - lastMonthCompletionRate;
        stats.put("completionGrowth", Math.round(completionGrowth * 100) / 100.0);
        
        return stats;
    }
    
    /**
     * 获取本月新增选题数
     */
    private long getCurrentMonthSelectionCount() {
        QueryWrapper<Selection> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }
    
    /**
     * 获取上月新增选题数
     */
    private long getLastMonthSelectionCount() {
        QueryWrapper<Selection> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }
    
    /**
     * 获取本月完成率
     */
    private double getCurrentMonthCompletionRate() {
        QueryWrapper<Selection> totalQuery = new QueryWrapper<>();
        totalQuery.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                  .eq("deleted", 0);
        long total = count(totalQuery);
        
        QueryWrapper<Selection> completedQuery = new QueryWrapper<>();
        completedQuery.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                     .eq("status", "completed")
                     .eq("deleted", 0);
        long completed = count(completedQuery);
        
        return total > 0 ? ((double)completed / total) * 100 : 0;
    }
    
    /**
     * 获取上月完成率
     */
    private double getLastMonthCompletionRate() {
        QueryWrapper<Selection> totalQuery = new QueryWrapper<>();
        totalQuery.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m')")
                  .eq("deleted", 0);
        long total = count(totalQuery);
        
        QueryWrapper<Selection> completedQuery = new QueryWrapper<>();
        completedQuery.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m')")
                     .eq("status", "completed")
                     .eq("deleted", 0);
        long completed = count(completedQuery);
        
        return total > 0 ? ((double)completed / total) * 100 : 0;
    }
}
