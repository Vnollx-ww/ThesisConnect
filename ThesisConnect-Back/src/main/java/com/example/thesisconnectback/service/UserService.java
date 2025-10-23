package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);

    /**
     * 根据角色查询用户列表
     */
    List<User> findByRole(String role);

    /**
     * 根据状态查询用户列表
     */
    List<User> findByStatus(Integer status);

    /**
     * 更新用户最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String newPassword);

    /**
     * 启用/禁用用户
     */
    boolean toggleUserStatus(Long userId);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats();

    /**
     * 批量删除用户
     */
    boolean batchDeleteUsers(List<Long> userIds);
}
