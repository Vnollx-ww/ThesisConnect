package com.example.thesisconnectback.mail;

import com.example.thesisconnectback.config.ThesisMailProperties;
import com.example.thesisconnectback.entity.Topic;
import com.example.thesisconnectback.entity.User;
import com.example.thesisconnectback.service.UserService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 基于 QQ SMTP 的 HTML 邮件发送；各场景使用独立 Thymeleaf 模板（templates/mail/*.html）
 */
@Service
public class MailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);

    private static final DateTimeFormatter CN_DATETIME = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm", Locale.CHINA);

    private final SpringTemplateEngine emailTemplateEngine;
    private final JavaMailSender mailSender;
    private final ThesisMailProperties thesisMail;
    private final String mailUsername;
    private final String mailPassword;
    private final UserService userService;

    public MailNotificationService(
            @Qualifier("emailTemplateEngine") SpringTemplateEngine emailTemplateEngine,
            JavaMailSender mailSender,
            ThesisMailProperties thesisMail,
            UserService userService,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.emailTemplateEngine = emailTemplateEngine;
        this.mailSender = mailSender;
        this.thesisMail = thesisMail;
        this.userService = userService;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    /**
     * 站点名、前端根地址及 hash 路由链接（与 router.js 中 path 一致）
     */
    private void applyCommonMailVariables(Context ctx) {
        ctx.setVariable("siteName", thesisMail.getSiteName());
        String base = thesisMail.getFrontendBaseUrl();
        if (base == null) {
            base = "";
        }
        base = base.trim().replaceAll("/+$", "");
        ctx.setVariable("frontendBaseUrl", base);
        boolean hasBase = StringUtils.hasText(base);
        ctx.setVariable("linksConfigured", hasBase);
        if (hasBase) {
            ctx.setVariable("linkLogin", base + "/#/login");
            ctx.setVariable("linkTeacherTopics", base + "/#/teacher/topics");
            ctx.setVariable("linkStudentTopics", base + "/#/student/topics");
            ctx.setVariable("linkStudentMyTopic", base + "/#/student/my-topic");
        } else {
            ctx.setVariable("linkLogin", "#");
            ctx.setVariable("linkTeacherTopics", "#");
            ctx.setVariable("linkStudentTopics", "#");
            ctx.setVariable("linkStudentMyTopic", "#");
        }
    }

    private boolean canSend() {
        return thesisMail.isEnabled()
                && StringUtils.hasText(mailUsername)
                && StringUtils.hasText(mailPassword);
    }

    private void sendHtml(String to, String subject, String templateName, Context ctx) {
        if (!canSend()) {
            log.debug("邮件未配置或已关闭，跳过: {}", subject);
            return;
        }
        if (!StringUtils.hasText(to)) {
            return;
        }
        try {
            applyCommonMailVariables(ctx);
            String html = emailTemplateEngine.process(templateName, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(new InternetAddress(mailUsername, thesisMail.getFromName(), StandardCharsets.UTF_8.name()));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("发送邮件失败 to={} subject={} : {}", to, subject, e.getMessage());
        }
    }

    /** 注册成功欢迎 */
    public void sendRegistrationWelcome(String toEmail, String realName) {
        Context ctx = new Context(Locale.CHINA);
        ctx.setVariable("realName", safe(realName));
        sendHtml(toEmail, "欢迎加入 " + thesisMail.getSiteName(), "register-welcome", ctx);
    }

    /** 学生提交选题 → 通知指导教师 */
    public void sendTeacherNewSelectionApplication(
            String teacherEmail,
            String teacherName,
            String studentName,
            String topicTitle,
            LocalDateTime submittedAt) {
        Context ctx = new Context(Locale.CHINA);
        ctx.setVariable("teacherName", safe(teacherName));
        ctx.setVariable("studentName", safe(studentName));
        ctx.setVariable("topicTitle", safe(topicTitle));
        ctx.setVariable("submittedAt", submittedAt == null ? "" : CN_DATETIME.format(submittedAt));
        sendHtml(teacherEmail, "【待审核】有新的选题申请 · " + thesisMail.getSiteName(), "selection-new-application-teacher", ctx);
    }

    /** 审核通过 → 通知学生 */
    public void sendStudentSelectionApproved(
            String studentEmail,
            String studentName,
            String topicTitle,
            String teacherName,
            String comment) {
        Context ctx = new Context(Locale.CHINA);
        ctx.setVariable("studentName", safe(studentName));
        ctx.setVariable("topicTitle", safe(topicTitle));
        ctx.setVariable("teacherName", safe(teacherName));
        ctx.setVariable("comment", safeComment(comment));
        sendHtml(studentEmail, "【已通过】您的选题申请已通过 · " + thesisMail.getSiteName(), "selection-approved-student", ctx);
    }

    /**
     * 教师/管理员发布新课题后，向所有状态正常且填写了邮箱的学生发送通知（每人一封，模板 topic-published-student）
     */
    public void notifyStudentsNewTopicPublished(Topic topic) {
        if (!canSend() || topic == null || topic.getId() == null) {
            log.debug("邮件未配置或课题信息不完整，跳过新课题广播");
            return;
        }
        List<User> students = userService.findByRole("student");
        int n = 0;
        for (User u : students) {
            if (u.getStatus() != null && u.getStatus() == 0) {
                continue;
            }
            if (!StringUtils.hasText(u.getEmail())) {
                continue;
            }
            Context ctx = new Context(Locale.CHINA);
            ctx.setVariable("studentName", safe(u.getRealName() != null ? u.getRealName() : u.getUsername()));
            ctx.setVariable("topicTitle", safe(topic.getTitle()));
            ctx.setVariable("teacherName", safe(topic.getTeacherName()));
            ctx.setVariable("major", safe(topic.getMajor()));
            ctx.setVariable("difficultyLabel", formatDifficulty(topic.getDifficulty()));
            ctx.setVariable("topicId", topic.getId());
            String subj = "【新课题】" + safe(topic.getTitle()) + " · " + thesisMail.getSiteName();
            sendHtml(u.getEmail(), subj, "topic-published-student", ctx);
            n++;
        }
        log.info("新课题发布邮件：已向 {} 名学生尝试发送（有邮箱且账号启用）", n);
    }

    private static String formatDifficulty(String d) {
        if (d == null || d.isEmpty()) {
            return "—";
        }
        return switch (d) {
            case "easy" -> "简单";
            case "medium" -> "中等";
            case "hard" -> "困难";
            default -> d;
        };
    }

    /** 审核拒绝 → 通知学生 */
    public void sendStudentSelectionRejected(
            String studentEmail,
            String studentName,
            String topicTitle,
            String teacherName,
            String comment) {
        Context ctx = new Context(Locale.CHINA);
        ctx.setVariable("studentName", safe(studentName));
        ctx.setVariable("topicTitle", safe(topicTitle));
        ctx.setVariable("teacherName", safe(teacherName));
        ctx.setVariable("comment", safeComment(comment));
        sendHtml(studentEmail, "【未通过】选题申请审核结果 · " + thesisMail.getSiteName(), "selection-rejected-student", ctx);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safeComment(String c) {
        if (c == null || c.isBlank()) {
            return "（无备注）";
        }
        return c.trim();
    }
}
