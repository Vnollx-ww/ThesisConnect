package com.example.thesisconnectback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.thesisconnectback.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统日志Mapper接口
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {

    /**
     * 根据用户ID查询日志
     */
    @Select("SELECT * FROM sys_log WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<SystemLog> findByUserId(@Param("userId") Long userId);

    /**
     * 根据操作类型查询日志
     */
    @Select("SELECT * FROM sys_log WHERE operation = #{operation} ORDER BY create_time DESC")
    List<SystemLog> findByOperation(@Param("operation") String operation);

    /**
     * 根据IP地址查询日志
     */
    @Select("SELECT * FROM sys_log WHERE ip = #{ip} ORDER BY create_time DESC")
    List<SystemLog> findByIp(@Param("ip") String ip);

    /**
     * 查询最近的活动日志
     */
    @Select("SELECT * FROM sys_log ORDER BY create_time DESC LIMIT #{limit}")
    List<SystemLog> findRecentLogs(@Param("limit") Integer limit);

    /**
     * 统计各操作类型数量
     */
    @Select("SELECT operation, COUNT(*) as count FROM sys_log GROUP BY operation")
    List<Object> countByOperation();

    /**
     * 统计用户活动数量
     */
    @Select("SELECT user_id, username, COUNT(*) as count FROM sys_log GROUP BY user_id, username ORDER BY count DESC")
    List<Object> countByUser();
}
