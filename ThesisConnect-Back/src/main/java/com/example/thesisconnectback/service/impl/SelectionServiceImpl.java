package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.mapper.SelectionMapper;
import com.example.thesisconnectback.mapper.TopicMapper;
import com.example.thesisconnectback.mapper.UserMapper;
import com.example.thesisconnectback.exception.BusinessException;
import com.example.thesisconnectback.service.ProgressChainService;
import com.example.thesisconnectback.service.SelectionNodeSubmissionService;
import com.example.thesisconnectback.service.SelectionService;
import com.example.thesisconnectback.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private ProgressChainService progressChainService;

    @Autowired
    private SelectionNodeSubmissionService selectionNodeSubmissionService;

    @Override
    public void validateSelectTopicRequest(Long studentId, Long topicId) {
        if (!systemConfigService.isWithinSelectionPeriod()) {
            throw new BusinessException(400, "当前不在选题开放时间内");
        }
        int max = systemConfigService.getMaxSelectionsPerStudent();
        if (selectionMapper.countNonRejectedByStudentId(studentId) >= max) {
            throw new BusinessException(400, "已达到最大选题申请数");
        }
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(400, "课题不存在");
        }
        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(400, "用户不存在");
        }
        if (!systemConfigService.isAllowCrossMajor()) {
            String sm = student.getMajor() != null ? student.getMajor().trim() : "";
            String tm = topic.getMajor() != null ? topic.getMajor().trim() : "";
            if (sm.isEmpty() || tm.isEmpty() || !sm.equalsIgnoreCase(tm)) {
                throw new BusinessException(400, "根据学院规定，不允许跨专业选题");
            }
        }
        int maxPerTeacher = systemConfigService.getMaxSelectionsPerTeacherPerStudent();
        if (selectionMapper.countNonRejectedByStudentAndTeacher(studentId, topic.getTeacherId()) >= maxPerTeacher) {
            throw new BusinessException(400, "已达到该指导教师名下的最大选题申请数");
        }
    }

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
    public Selection selectTopic(Long studentId, Long topicId) {
        // 锁定课题行，避免并发下超额确认
        Topic topic = topicMapper.selectByIdForUpdate(topicId);
        if (topic == null) {
            throw new BusinessException(400, "课题不存在");
        }

        if (isTopicFull(topicId)) {
            throw new BusinessException(400, "该课题已满员");
        }

        if (selectionMapper.countRejectedByStudentAndTopic(studentId, topicId) > 0) {
            throw new BusinessException(400, "您已被该课题拒绝，无法再次申请");
        }

        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(400, "用户不存在");
        }

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

        try {
            if (!save(selection)) {
                throw new BusinessException(400, "选题失败");
            }
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(400, "选题失败，请勿重复提交或稍后再试");
        }

        return selection;
    }

    @Override
    @Transactional
    public boolean cancelSelection(Long selectionId) {
        Selection selection = getById(selectionId);
        // 允许撤销待审核、已通过待确认的申请，或删除已拒绝的记录
        if (selection != null && ("pending".equals(selection.getStatus()) || "approved".equals(selection.getStatus()) || "rejected".equals(selection.getStatus()))) {
            Long topicId = selection.getTopicId();
            boolean removed = removeById(selectionId);
            
            // 如果删除成功，更新课题的已选人数（重新计算已确认的人数）
            if (removed) {
                int confirmedCount = selectionMapper.countConfirmedByTopicId(topicId);
                topicMapper.updateSelectedCount(topicId, confirmedCount);
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
            
            // 与 isTopicFull 一致：课题容量按「已通过」申请数统计
            if (updated) {
                Long topicId = selection.getTopicId();
                int approvedCount = selectionMapper.countApprovedByTopicId(topicId);
                topicMapper.updateSelectedCount(topicId, approvedCount);
            }
            
            return updated;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateSelectionStatus(Long selectionId, String status) {
        Selection selection = getById(selectionId);
        if (selection != null) {
            selection.setStatus(status);
            selection.setUpdateTime(LocalDateTime.now());
            boolean updated = updateById(selection);

            if (updated && "confirmed".equals(status)) {
                Long topicId = selection.getTopicId();
                int confirmedCount = selectionMapper.countConfirmedByTopicId(topicId);
                topicMapper.updateSelectedCount(topicId, confirmedCount);
            }

            if (updated && ("confirmed".equals(status) || "active".equals(status))) {
                Selection fresh = getById(selectionId);
                if (fresh != null && fresh.getProgressChainId() == null) {
                    progressChainService.attachDefaultChainToSelection(fresh);
                }
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
    public boolean gradeSelection(Long selectionId, String grade, String evaluation) {
        Selection selection = getById(selectionId);
        if (selection != null) {
            selection.setFinalGrade(grade);
            selection.setTeacherEvaluation(evaluation);
            selection.setStatus("completed"); // 确保状态为已完成
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
            int pipeline = selectionMapper.countPipelineByTopicId(topicId);
            return pipeline >= topic.getMaxStudents();
        }
        return true;
    }

    @Override
    public Map<String, Object> batchReviewSelections(Long reviewerUserId, String reviewerRole, List<Long> selectionIds, String status, String comment) {
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failures = new ArrayList<>();
        Map<String, Object> out = new HashMap<>();
        out.put("successIds", successIds);
        out.put("failures", failures);
        if (selectionIds == null || selectionIds.isEmpty()) {
            return out;
        }
        for (Long id : selectionIds) {
            if (id == null) {
                continue;
            }
            Selection sel = getById(id);
            if (sel == null) {
                failures.add(batchFailure(id, "选题记录不存在"));
                continue;
            }
            if (!"teacher".equals(reviewerRole) && !"admin".equals(reviewerRole)) {
                failures.add(batchFailure(id, "权限不足"));
                continue;
            }
            if ("teacher".equals(reviewerRole) && (reviewerUserId == null || !reviewerUserId.equals(sel.getTeacherId()))) {
                failures.add(batchFailure(id, "只能审核本人课题的申请"));
                continue;
            }
            if (!"pending".equals(sel.getStatus())) {
                failures.add(batchFailure(id, "仅待审核记录可审核"));
                continue;
            }
            if (status == null || (!"approved".equals(status) && !"rejected".equals(status))) {
                failures.add(batchFailure(id, "审核状态无效"));
                continue;
            }
            boolean ok = reviewSelection(id, status, comment);
            if (ok) {
                successIds.add(id);
            } else {
                failures.add(batchFailure(id, "审核失败"));
            }
        }
        return out;
    }

    private static Map<String, Object> batchFailure(Long id, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("message", message);
        return m;
    }

    @Override
    public List<User> getStudentsByTopicId(Long topicId) {
        return selectionMapper.findStudentsByTopicId(topicId);
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

    private static int clampListLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    /**
     * Get recent selections
     */
    @Override
    public List<Map<String, Object>> getRecentSelections(int limit) {
        int safeLimit = clampListLimit(limit);
        QueryWrapper<Selection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("create_time")
                   .last("LIMIT " + safeLimit);
        List<Selection> selections = list(queryWrapper);
        return selections.stream().map(selection -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", selection.getId());
            map.put("student_id", selection.getStudentId());
            map.put("student_name", selection.getStudentName());
            map.put("student_number", selection.getStudentNumber());
            map.put("topic_id", selection.getTopicId());
            map.put("topic_title", selection.getTopicTitle());
            map.put("status", selection.getStatus());
            map.put("create_time", selection.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean adjustProgressChainStep(Long selectionId, String action, Integer setCount, Long operatorUserId, String operatorRole) {
        Selection selection = getById(selectionId);
        if (selection == null || selection.getProgressChainId() == null) {
            return false;
        }
        if (!"admin".equals(operatorRole)) {
            if ("teacher".equals(operatorRole)) {
                if (operatorUserId == null || !operatorUserId.equals(selection.getTeacherId())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (!"confirmed".equals(selection.getStatus()) && !"active".equals(selection.getStatus())) {
            return false;
        }

        int n = progressChainService.countNodes(selection.getProgressChainId());
        int c = selection.getProgressCompletedCount() != null ? selection.getProgressCompletedCount() : 0;

        if ("next".equals(action) && "teacher".equals(operatorRole)) {
            if (c < n && !selectionNodeSubmissionService.isLatestApprovedForNode(selectionId, c)) {
                throw new BusinessException(400, "请先在「课题进度」中审核通过学生本阶段提交的材料后，再点击「完成本阶段」推进");
            }
        }

        if ("next".equals(action)) {
            c = Math.min(n, c + 1);
        } else if ("prev".equals(action)) {
            c = Math.max(0, c - 1);
        } else if ("set".equals(action) && setCount != null) {
            c = Math.max(0, Math.min(n, setCount));
        } else {
            return false;
        }

        selection.setProgressCompletedCount(c);
        selection.setProgress(computeChainProgressPercent(c, n));
        selection.setUpdateTime(LocalDateTime.now());
        return updateById(selection);
    }

    private static int computeChainProgressPercent(int completedCount, int totalNodes) {
        if (totalNodes <= 0) {
            return 0;
        }
        int c = Math.max(0, Math.min(completedCount, totalNodes));
        if (c >= totalNodes) {
            return 100;
        }
        return (int) (c * 100L / totalNodes);
    }

    @Override
    public Map<String, Object> getProgressChainView(Long selectionId, Long requestUserId, String role) {
        Selection selection = getById(selectionId);
        if (selection == null) {
            Map<String, Object> m = new HashMap<>();
            m.put("_notFound", Boolean.TRUE);
            return m;
        }
        if ("student".equals(role)) {
            if (requestUserId == null || !requestUserId.equals(selection.getStudentId())) {
                return null;
            }
        } else if ("teacher".equals(role)) {
            if (requestUserId == null || !requestUserId.equals(selection.getTeacherId())) {
                return null;
            }
        } else if (!"admin".equals(role)) {
            return null;
        }
        return progressChainService.buildChainView(selection);
    }
}
