package com.example.thesisconnectback.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            // 角色筛选
            if (role != null && !role.isEmpty()) {
                queryWrapper.eq("role", role);
            }

            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like("username", keyword)
                        .or()
                        .like("real_name", keyword)
                        .or()
                        .like("student_id", keyword)
                );
            }

            queryWrapper.orderByDesc("create_time");
            Page<User> result = userService.page(pageParam, queryWrapper);

            // 清除密码信息
            result.getRecords().forEach(user -> user.setPassword(null));

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败：", e);
            return Result.error("获取用户列表失败");
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user != null) {
                user.setPassword(null);
                return Result.success(user);
            } else {
                return Result.notFound("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户详情失败：", e);
            return Result.error("获取用户详情失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<User> createUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以创建用户）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 加密密码
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            user.setStatus(1);
            user.setLoginCount(0);

            boolean success = userService.save(user);
            if (success) {
                user.setPassword(null);
                return Result.success("用户创建成功", user);
            } else {
                return Result.error("用户创建失败");
            }
        } catch (Exception e) {
            log.error("创建用户失败：", e);
            return Result.error("创建用户失败");
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        try {
            // 检查权限
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            
            if (!"admin".equals(role) && !userId.equals(id)) {
                return Result.forbidden("权限不足");
            }

            User existingUser = userService.getById(id);
            if (existingUser == null) {
                return Result.notFound("用户不存在");
            }

            // 更新用户信息
            existingUser.setRealName(user.getRealName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setDepartment(user.getDepartment());
            existingUser.setStudentId(user.getStudentId());
            existingUser.setAvatar(user.getAvatar());

            // 只有管理员可以修改角色和状态
            if ("admin".equals(role)) {
                existingUser.setRole(user.getRole());
                existingUser.setStatus(user.getStatus());
            }

            boolean success = userService.updateById(existingUser);
            if (success) {
                existingUser.setPassword(null);
                return Result.success("用户信息更新成功", existingUser);
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败：", e);
            return Result.error("更新用户信息失败");
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以删除用户）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            boolean success = userService.removeById(id);
            if (success) {
                return Result.success("用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败：", e);
            return Result.error("删除用户失败");
        }
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> userIds, HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以批量删除用户）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            boolean success = userService.batchDeleteUsers(userIds);
            if (success) {
                return Result.success("批量删除用户成功");
            } else {
                return Result.error("批量删除用户失败");
            }
        } catch (Exception e) {
            log.error("批量删除用户失败：", e);
            return Result.error("批量删除用户失败");
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> passwordForm, HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以重置密码）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            String newPassword = passwordForm.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return Result.badRequest("新密码不能为空");
            }

            boolean success = userService.resetPassword(id, newPassword);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置密码失败：", e);
            return Result.error("重置密码失败");
        }
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/{id}/toggle-status")
    public Result<Void> toggleUserStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以修改用户状态）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            boolean success = userService.toggleUserStatus(id);
            if (success) {
                return Result.success("用户状态修改成功");
            } else {
                return Result.error("用户状态修改失败");
            }
        } catch (Exception e) {
            log.error("修改用户状态失败：", e);
            return Result.error("修改用户状态失败");
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats(HttpServletRequest request) {
        try {
            // 检查权限（只有管理员可以查看统计信息）
            String role = (String) request.getAttribute("role");
            if (!"admin".equals(role)) {
                return Result.forbidden("权限不足");
            }

            Map<String, Object> stats = userService.getUserStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败：", e);
            return Result.error("获取用户统计信息失败");
        }
    }
}
