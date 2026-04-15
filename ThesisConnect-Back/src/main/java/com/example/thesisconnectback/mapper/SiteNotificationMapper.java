package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.SiteNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SiteNotificationMapper extends BaseMapper<SiteNotification> {

    @Select("SELECT COUNT(*) FROM sys_site_notification WHERE user_id = #{userId} AND read_flag = 0")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Update("UPDATE sys_site_notification SET read_flag = 1 WHERE user_id = #{userId} AND read_flag = 0")
    int markAllRead(@Param("userId") Long userId);
}
