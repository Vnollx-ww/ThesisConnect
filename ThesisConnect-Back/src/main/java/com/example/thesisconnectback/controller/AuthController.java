package com.example.thesisconnectback.controller;

import com.example.thesisconnectback.common.Result;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.service.UserService;
import com.example.thesisconnectback.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> registerForm) {
        String username = registerForm.get("username");
        String password = registerForm.get("password");
        String confirmPassword = registerForm.get("confirmPassword");
        String role = registerForm.get("role");
        String realName = registerForm.get("realName");
        String email = registerForm.get("email");
        String phone = registerForm.get("phone");
        String department = registerForm.get("department");
        String studentId = registerForm.get("studentId");

        // 验证必填字段
        if (username == null || password == null || role == null || realName == null) {
            return Result.badRequest("用户名、密码、角色和姓名不能为空");
        }

        // 验证密码确认
        if (!password.equals(confirmPassword)) {
            return Result.badRequest("两次输入的密码不一致");
        }

        // 验证密码长度
        if (password.length() < 6) {
            return Result.badRequest("密码长度不能少于6位");
        }

        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(username);
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 创建新用户
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(role);
            newUser.setRealName(realName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setDepartment(department);
            newUser.setStudentId(studentId);
            newUser.setStatus(1); // 默认启用
            newUser.setLoginCount(0);

            boolean success = userService.register(newUser);
            if (success) {
                // 清除密码信息
                newUser.setPassword(null);
                return Result.success("注册成功", newUser);
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            log.error("注册失败：", e);
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        String role = loginForm.get("role");

        if (username == null || password == null || role == null) {
            return Result.badRequest("用户名、密码和角色不能为空");
        }

        try {
            User user = userService.login(username, password);
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 验证角色
            if (!role.equals(user.getRole())) {
                return Result.error("角色不匹配");
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                return Result.error("用户已被禁用");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 返回用户信息和token
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            data.put("role", user.getRole());

            return Result.success("登录成功", data);
        } catch (Exception e) {
            log.error("登录失败：", e);
            return Result.error("登录失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User user = userService.getById(userId);
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                return Result.success(user);
            } else {
                return Result.notFound("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败：", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/userinfo")
    public Result<User> updateUserInfo(@RequestBody Map<String, Object> userData, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            User existingUser = userService.getById(userId);
            
            if (existingUser == null) {
                return Result.notFound("用户不存在");
            }

            // 更新用户信息（只更新User实体类中实际存在的字段）
            if (userData.containsKey("realName")) {
                existingUser.setRealName((String) userData.get("realName"));
            }
            if (userData.containsKey("email")) {
                existingUser.setEmail((String) userData.get("email"));
            }
            if (userData.containsKey("phone")) {
                existingUser.setPhone((String) userData.get("phone"));
            }
            if (userData.containsKey("department")) {
                existingUser.setDepartment((String) userData.get("department"));
            }
            if (userData.containsKey("studentId")) {
                existingUser.setStudentId((String) userData.get("studentId"));
            }
            if (userData.containsKey("avatar")) {
                existingUser.setAvatar((String) userData.get("avatar"));
            }

            boolean success = userService.updateById(existingUser);
            if (success) {
                // 清除敏感信息
                existingUser.setPassword(null);
                return Result.success(existingUser, "用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败：", e);
            return Result.error("更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT是无状态的，登出只需要前端删除token即可
        return Result.success("登出成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> passwordForm, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            String oldPassword = passwordForm.get("oldPassword");
            String newPassword = passwordForm.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Result.badRequest("旧密码和新密码不能为空");
            }

            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败，请检查旧密码是否正确");
            }
        } catch (Exception e) {
            log.error("修改密码失败：", e);
            return Result.error("修改密码失败");
        }
    }
}
