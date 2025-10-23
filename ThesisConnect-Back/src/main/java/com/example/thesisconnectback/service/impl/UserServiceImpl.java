package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.mapper.UserMapper;
import com.example.thesisconnectback.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean register(User user) {
        try {
            // 加密密码
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            // 设置默认值
            user.setStatus(1);
            user.setLoginCount(0);
            return save(user);
        } catch (Exception e) {
            throw new RuntimeException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            // 更新登录信息
            updateLastLoginTime(user.getId());
            return user;
        }
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public List<User> findByRole(String role) {
        return userMapper.findByRole(role);
    }

    @Override
    public List<User> findByStatus(Integer status) {
        return userMapper.findByStatus(status);
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        userMapper.updateLastLoginTime(userId);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user != null && BCrypt.checkpw(oldPassword, user.getPassword())) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            return updateById(user);
        }
        return false;
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user != null) {
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            return updateById(user);
        }
        return false;
    }

    @Override
    public boolean toggleUserStatus(Long userId) {
        User user = getById(userId);
        if (user != null) {
            user.setStatus(user.getStatus() == 1 ? 0 : 1);
            return updateById(user);
        }
        return false;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = count();
        stats.put("totalUsers", totalUsers);
        
        // 各角色用户数
        List<User> students = findByRole("student");
        List<User> teachers = findByRole("teacher");
        List<User> admins = findByRole("admin");
        
        stats.put("students", students.size());
        stats.put("teachers", teachers.size());
        stats.put("admins", admins.size());
        
        // 活跃用户数
        List<User> activeUsers = findByStatus(1);
        stats.put("activeUsers", activeUsers.size());
        
        return stats;
    }

    @Override
    @Transactional
    public boolean batchDeleteUsers(List<Long> userIds) {
        return removeByIds(userIds);
    }

    @Override
    public Map<String, Object> getUserGrowthTrend(String period) {
        Map<String, Object> trendData = new HashMap<>();
        
        // 根据时间段获取用户增长数据
        List<Map<String, Object>> growthData = userMapper.getUserGrowthTrend(period);
        
        // 构建时间轴和用户数量数据
        List<String> timeLabels = new ArrayList<>();
        List<Integer> userCounts = new ArrayList<>();
        
        for (Map<String, Object> data : growthData) {
            timeLabels.add((String) data.get("timeLabel"));
            userCounts.add(((Number) data.get("userCount")).intValue());
        }
        
        trendData.put("timeLabels", timeLabels);
        trendData.put("userCounts", userCounts);
        trendData.put("period", period);
        
        return trendData;
    }
    
    @Override
    public List<Map<String, Object>> getStudentsByTeacherWithSelection(Long teacherId) {
        return userMapper.getStudentsByTeacherWithSelection(teacherId);
    }
    
    @Override
    public Map<String, Object> getStudentStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取总学生数
        QueryWrapper<User> totalQuery = new QueryWrapper<>();
        totalQuery.eq("role", "student").eq("deleted", 0);
        long totalStudents = count(totalQuery);
        
        // 获取活跃学生数（有选题的学生）
        List<User> activeStudents = userMapper.getActiveStudents();
        
        // 获取已完成学生数
        List<User> completedStudents = userMapper.getCompletedStudents();
        
        // 计算平均进度
        Double avgProgress = userMapper.getAverageProgress();
        
        stats.put("totalStudents", totalStudents);
        stats.put("activeStudents", activeStudents.size());
        stats.put("completedStudents", completedStudents.size());
        stats.put("avgProgress", avgProgress != null ? avgProgress : 0);
        
        return stats;
    }
    
    /**
     * 根据教师获取学生统计信息
     */
    public Map<String, Object> getStudentStatsByTeacher(Long teacherId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取选择该教师课题的学生总数
        List<Map<String, Object>> students = userMapper.getStudentsByTeacherWithSelection(teacherId);
        int totalStudents = students.size();
        
        // 统计活跃学生（状态为active或approved）
        int activeStudents = 0;
        int completedStudents = 0;
        double totalProgress = 0;
        
        for (Map<String, Object> student : students) {
            String status = (String) student.get("selection_status");
            Integer progress = (Integer) student.get("progress");
            
            if ("active".equals(status) || "approved".equals(status)) {
                activeStudents++;
                if (progress != null) {
                    totalProgress += progress;
                }
            } else if ("completed".equals(status)) {
                completedStudents++;
            }
        }
        
        double avgProgress = totalStudents > 0 ? totalProgress / totalStudents : 0;
        
        stats.put("totalStudents", totalStudents);
        stats.put("activeStudents", activeStudents);
        stats.put("completedStudents", completedStudents);
        stats.put("avgProgress", Math.round(avgProgress * 100) / 100.0);
        
        return stats;
    }
}
