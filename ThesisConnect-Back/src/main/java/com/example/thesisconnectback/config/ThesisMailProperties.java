package com.example.thesisconnectback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 业务邮件展示名与开关（SMTP 账号仍使用 spring.mail.*）
 */
@Data
@ConfigurationProperties(prefix = "thesis.mail")
public class ThesisMailProperties {

    /** 为 false 时不发送（仍可用于本地调试） */
    private boolean enabled = true;

    /** 发件人「姓名」显示在收件箱中 */
    private String fromName = "ThesisConnect";

    /** 邮件内品牌名、页脚文案 */
    private String siteName = "ThesisConnect";

    /**
     * 前端访问根地址（不含末尾 /），邮件内按钮会拼接为 {@code 该地址/#/login} 等（与当前 Vue hash 路由一致）。
     * 可用环境变量 FRONTEND_BASE_URL 覆盖。
     */
    private String frontendBaseUrl = "http://localhost:3030";
}
