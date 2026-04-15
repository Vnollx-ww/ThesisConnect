package com.example.thesisconnectback.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 */
@Configuration
public class MybatisPlusConfig {

    private static final Logger log = LoggerFactory.getLogger(MybatisPlusConfig.class);

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }

    @Bean
    public ApplicationRunner selectionSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_selection",
                    "teacher_evaluation",
                    "ADD COLUMN teacher_evaluation TEXT COMMENT '教师评价' AFTER final_grade"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "gender",
                    "ADD COLUMN gender VARCHAR(20) COMMENT '性别' AFTER avatar"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "birthday",
                    "ADD COLUMN birthday VARCHAR(20) COMMENT '生日' AFTER gender"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "address",
                    "ADD COLUMN address VARCHAR(255) COMMENT '地址' AFTER birthday"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "major",
                    "ADD COLUMN major VARCHAR(100) COMMENT '专业' AFTER address"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "class_name",
                    "ADD COLUMN class_name VARCHAR(100) COMMENT '班级名称' AFTER major"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_user",
                    "title",
                    "ADD COLUMN title VARCHAR(100) COMMENT '职称/职位' AFTER class_name"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_progress",
                    "report_url",
                    "ADD COLUMN report_url VARCHAR(500) COMMENT '报告文档URL' AFTER milestone_date"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_progress",
                    "status",
                    "ADD COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending-待审核，approved-已通过，rejected-已拒绝' AFTER report_url"
            );
            ensureColumnExists(
                    jdbcTemplate,
                    "sys_progress",
                    "reject_reason",
                    "ADD COLUMN reject_reason TEXT COMMENT '拒绝原因' AFTER status"
            );

            ensureSystemConfigTable(jdbcTemplate);
            ensureSelectionUniqueStudentTopic(jdbcTemplate);
        };
    }

    private void ensureSelectionUniqueStudentTopic(JdbcTemplate jdbcTemplate) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_selection'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            return;
        }
        Integer idx = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sys_selection' AND index_name = 'uk_student_topic'",
                Integer.class
        );
        if (idx != null && idx > 0) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE sys_selection ADD UNIQUE INDEX uk_student_topic (student_id, topic_id)");
            log.info("已添加 sys_selection.uk_student_topic 唯一索引");
        } catch (Exception e) {
            log.warn("添加 uk_student_topic 索引跳过: {}", e.getMessage());
        }
    }

    private void ensureSystemConfigTable(JdbcTemplate jdbcTemplate) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_system_config'",
                Integer.class
        );
        if (tableExists == null || tableExists == 0) {
            jdbcTemplate.execute(
                    "CREATE TABLE sys_system_config ("
                            + "config_key VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '配置键',"
                            + "config_value TEXT COMMENT '配置值',"
                            + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统键值配置'"
            );
            log.info("已创建 sys_system_config 表");
        }
        Integer rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_system_config", Integer.class);
        if (rowCount != null && rowCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO sys_system_config (config_key, config_value) VALUES (?, ?), (?, ?), (?, ?)",
                    "selection_start_time", "2026-02-01T00:00:00",
                    "selection_end_time", "2026-12-31T23:59:59",
                    "max_selections_per_student", "3"
            );
            log.info("已写入默认系统配置");
        }
    }

    private void ensureColumnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName, String columnDefinition) {
        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );

        if (tableExists == null || tableExists == 0) {
            log.warn("表 {} 不存在，跳过 {} 字段检查", tableName, columnName);
            return;
        }

        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );

        if (columnExists == null || columnExists == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " " + columnDefinition);
            log.info("已为 {} 表补充 {} 字段", tableName, columnName);
        }
    }
}
