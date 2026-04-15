package com.example.thesisconnectback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.entity.Selection;
import com.example.thesisconnectback.mapper.UserMapper;
import com.example.thesisconnectback.service.UserService;
import com.example.thesisconnectback.service.SelectionService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SelectionService selectionService;

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
        
        // 计算用户增长率（本月新增用户数 / 上月新增用户数）
        long currentMonthUsers = getCurrentMonthUserCount();
        long lastMonthUsers = getLastMonthUserCount();
        double userGrowth = lastMonthUsers > 0 ? ((double)(currentMonthUsers - lastMonthUsers) / lastMonthUsers) * 100 : 0;
        stats.put("userGrowth", Math.round(userGrowth * 100) / 100.0);
        
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
        List<Map<String, Object>> students = userMapper.getStudentsByTeacherWithSelection(teacherId);
        if (students != null) {
            for (Map<String, Object> student : students) {
                if (!student.containsKey("avatar")) {
                    student.put("avatar", null);
                }
            }
        }
        return students;
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
        
        // 获取选择该教师课题的学生总数（Mapper中已过滤掉rejected状态）
        List<Map<String, Object>> students = userMapper.getStudentsByTeacherWithSelection(teacherId);
        
        // 再次过滤，确保不包含rejected状态（双重保险）
        long validStudentCount = students.stream()
                .filter(s -> !"rejected".equals(s.get("selection_status")))
                .count();
                
        int totalStudents = (int) validStudentCount;
        
        // 统计活跃学生（状态为active或approved或confirmed）
        int activeStudents = 0;
        int completedStudents = 0;
        double totalProgress = 0;
        
        for (Map<String, Object> student : students) {
            String status = (String) student.get("selection_status");
            
            // 跳过已拒绝的（虽然理论上不应该有）
            if ("rejected".equals(status)) {
                continue;
            }
            
            Integer progress = (Integer) student.get("progress");
            
            if ("active".equals(status) || "approved".equals(status) || "confirmed".equals(status)) {
                activeStudents++;
                if (progress != null) {
                    totalProgress += progress;
                }
            } else if ("completed".equals(status)) {
                completedStudents++;
                // 完成的学生进度算100%
                totalProgress += 100;
            }
        }
        
        double avgProgress = totalStudents > 0 ? totalProgress / totalStudents : 0;
        
        stats.put("totalStudents", totalStudents);
        stats.put("activeStudents", activeStudents);
        stats.put("completedStudents", completedStudents);
        stats.put("avgProgress", Math.round(avgProgress * 100) / 100.0);
        
        return stats;
    }
    
    /**
     * 获取本月新增用户数
     */
    private long getCurrentMonthUserCount() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }
    
    /**
     * 获取上月新增用户数
     */
    private long getLastMonthUserCount() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m')")
                   .eq("deleted", 0);
        return count(queryWrapper);
    }

    private static int clampListLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    /**
     * Get recent users
     */
    @Override
    public List<Map<String, Object>> getRecentUsers(int limit) {
        int safe = clampListLimit(limit);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                   .orderByDesc("create_time")
                   .last("LIMIT " + safe);
        List<User> users = list(queryWrapper);
        return users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("real_name", user.getRealName());
            map.put("role", user.getRole());
            map.put("create_time", user.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public int importUsers(MultipartFile file) {
        int count = 0;
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                User user = new User();
                user.setUsername(getCellValue(row.getCell(0)));
                user.setRealName(getCellValue(row.getCell(1)));
                user.setRole(getCellValue(row.getCell(2)));
                user.setEmail(getCellValue(row.getCell(3)));
                user.setPhone(getCellValue(row.getCell(4)));
                user.setStudentId(getCellValue(row.getCell(5)));
                user.setDepartment(getCellValue(row.getCell(6)));
                
                String password = getCellValue(row.getCell(7));
                if (password == null || password.isEmpty()) {
                    password = "123456";
                }
                user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                user.setStatus(1);
                user.setLoginCount(0);
                
                // Check if username already exists
                if (findByUsername(user.getUsername()) == null) {
                    save(user);
                    count++;
                }
            }
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage());
        }
        return count;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            default: return null;
        }
    }

    @Override
    public List<Map<String, Object>> getUserActivities(Long userId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        User user = getById(userId);
        if (user == null) return activities;

        // 用户注册活动
        Map<String, Object> regActivity = new HashMap<>();
        regActivity.put("title", "账号注册");
        regActivity.put("description", user.getRealName() + " 完成了账号注册");
        regActivity.put("time", user.getCreateTime());
        regActivity.put("type", "register");
        activities.add(regActivity);

        // If student, add topic selection activities
        if ("student".equals(user.getRole())) {
            QueryWrapper<com.example.thesisconnectback.entity.Selection> selQuery = new QueryWrapper<>();
            selQuery.eq("student_id", userId).orderByDesc("create_time").last("LIMIT 10");
            List<com.example.thesisconnectback.entity.Selection> selections = selectionService.list(selQuery);
            for (com.example.thesisconnectback.entity.Selection sel : selections) {
                Map<String, Object> act = new HashMap<>();
                act.put("title", "课题选择");
                act.put("description", "选择了课题编号 " + sel.getTopicId() + "，当前状态：" + sel.getStatus());
                act.put("time", sel.getCreateTime());
                act.put("type", "selection");
                activities.add(act);
            }
        }

        // Sort by time
        activities.sort((a, b) -> {
            Object t1 = a.get("time");
            Object t2 = b.get("time");
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.toString().compareTo(t1.toString());
        });

        return activities;
    }
}
