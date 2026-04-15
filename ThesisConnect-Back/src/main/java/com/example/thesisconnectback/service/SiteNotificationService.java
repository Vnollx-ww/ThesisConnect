package com.example.thesisconnectback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.thesisconnectback.entity.SiteNotification;

import java.util.Map;

public interface SiteNotificationService extends IService<SiteNotification> {

    void notifyUser(Long userId, String bizType, String title, String content, String relatedType, Long relatedId);

    Page<SiteNotification> pageForUser(Long userId, int page, int size, String bizType, Boolean read);

    boolean markRead(Long userId, Long id);

    int markAllRead(Long userId);

    long unreadCount(Long userId);

    /** 待办汇总：教师待审、学生待确认等 */
    Map<String, Long> pendingSummary(Long userId, String role);
}
