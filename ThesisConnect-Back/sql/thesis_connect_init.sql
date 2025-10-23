-- 毕业论文选题双向选择系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS thesis_connect DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE thesis_connect;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色：student-学生，teacher-教师，admin-管理员',
    department VARCHAR(100) COMMENT '部门/专业',
    student_id VARCHAR(50) COMMENT '学号/工号',
    avatar VARCHAR(255) COMMENT '头像',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    login_count INT DEFAULT 0 COMMENT '登录次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 课题表
CREATE TABLE IF NOT EXISTS sys_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课题ID',
    title VARCHAR(200) NOT NULL COMMENT '课题标题',
    description TEXT COMMENT '课题描述',
    teacher_id BIGINT NOT NULL COMMENT '指导教师ID',
    teacher_name VARCHAR(50) NOT NULL COMMENT '指导教师姓名',
    major VARCHAR(100) NOT NULL COMMENT '专业要求',
    difficulty VARCHAR(20) NOT NULL DEFAULT 'medium' COMMENT '难度等级：easy-简单，medium-中等，hard-困难',
    max_students INT NOT NULL DEFAULT 2 COMMENT '最大学生数',
    selected_count INT NOT NULL DEFAULT 0 COMMENT '已选学生数',
    requirements TEXT COMMENT '技术要求',
    expected_outcome TEXT COMMENT '预期成果',
    deadline DATETIME COMMENT '截止时间',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-进行中，completed-已完成，paused-已暂停',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    rating DECIMAL(3,1) DEFAULT 0.0 COMMENT '评分',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_major (major),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课题表';

-- 选题记录表
CREATE TABLE IF NOT EXISTS sys_selection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选题ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    student_number VARCHAR(50) COMMENT '学生学号',
    topic_id BIGINT NOT NULL COMMENT '课题ID',
    topic_title VARCHAR(200) NOT NULL COMMENT '课题标题',
    teacher_id BIGINT NOT NULL COMMENT '指导教师ID',
    teacher_name VARCHAR(50) NOT NULL COMMENT '指导教师姓名',
    selection_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选题时间',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已拒绝',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    progress_description TEXT COMMENT '进度描述',
    problems TEXT COMMENT '遇到的问题',
    final_grade VARCHAR(10) COMMENT '最终成绩',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_student_id (student_id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (student_id) REFERENCES sys_user(id),
    FOREIGN KEY (topic_id) REFERENCES sys_topic(id),
    FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选题记录表';

-- 文档表
CREATE TABLE IF NOT EXISTS sys_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    name VARCHAR(200) NOT NULL COMMENT '文档名称',
    original_name VARCHAR(200) NOT NULL COMMENT '原始文件名',
    type VARCHAR(50) NOT NULL COMMENT '文档类型',
    size BIGINT NOT NULL COMMENT '文件大小（字节）',
    path VARCHAR(500) NOT NULL COMMENT '文件路径',
    extension VARCHAR(20) COMMENT '文件扩展名',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    uploader_name VARCHAR(50) NOT NULL COMMENT '上传者姓名',
    selection_id BIGINT COMMENT '关联的选题ID',
    topic_id BIGINT COMMENT '关联的课题ID',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已拒绝',
    review_comment TEXT COMMENT '审核意见',
    reviewer_id BIGINT COMMENT '审核人ID',
    reviewer_name VARCHAR(50) COMMENT '审核人姓名',
    review_time DATETIME COMMENT '审核时间',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_uploader_id (uploader_id),
    INDEX idx_selection_id (selection_id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (uploader_id) REFERENCES sys_user(id),
    FOREIGN KEY (selection_id) REFERENCES sys_selection(id),
    FOREIGN KEY (topic_id) REFERENCES sys_topic(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- 进度记录表
CREATE TABLE IF NOT EXISTS sys_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '进度ID',
    selection_id BIGINT NOT NULL COMMENT '选题ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    topic_id BIGINT NOT NULL COMMENT '课题ID',
    percentage INT NOT NULL DEFAULT 0 COMMENT '进度百分比',
    description TEXT COMMENT '进度描述',
    problems TEXT COMMENT '遇到的问题',
    milestone_title VARCHAR(200) COMMENT '里程碑标题',
    milestone_description TEXT COMMENT '里程碑描述',
    milestone_status VARCHAR(20) DEFAULT 'pending' COMMENT '里程碑状态：completed-已完成，current-进行中，pending-待开始',
    milestone_date DATETIME COMMENT '里程碑日期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_selection_id (selection_id),
    INDEX idx_student_id (student_id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_milestone_status (milestone_status),
    INDEX idx_deleted (deleted),
    FOREIGN KEY (selection_id) REFERENCES sys_selection(id),
    FOREIGN KEY (student_id) REFERENCES sys_user(id),
    FOREIGN KEY (topic_id) REFERENCES sys_topic(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进度记录表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    description TEXT COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    time BIGINT COMMENT '执行时间（毫秒）',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 插入初始数据
-- 插入管理员用户
INSERT INTO sys_user (username, password, real_name, email, phone, role, department, student_id, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 'admin@thesisconnect.com', '13800000000', 'admin', '系统管理部', 'ADMIN001', 1);

-- 插入教师用户
INSERT INTO sys_user (username, password, real_name, email, phone, role, department, student_id, status) VALUES
('teacher001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '李教授', 'li.professor@university.edu.cn', '13800000001', 'teacher', '计算机科学与技术学院', 'T2021001', 1),
('teacher002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '王老师', 'wang.teacher@university.edu.cn', '13800000002', 'teacher', '软件工程学院', 'T2021002', 1),
('teacher003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '张教授', 'zhang.professor@university.edu.cn', '13800000003', 'teacher', '网络工程学院', 'T2021003', 1);

-- 插入学生用户
INSERT INTO sys_user (username, password, real_name, email, phone, role, department, student_id, status) VALUES
('student001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '张三', 'zhangsan@student.edu.cn', '13800000004', 'student', '计算机科学与技术', '2021001001', 1),
('student002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '李四', 'lisi@student.edu.cn', '13800000005', 'student', '软件工程', '2021001002', 1),
('student003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '王五', 'wangwu@student.edu.cn', '13800000006', 'student', '网络工程', '2021001003', 1),
('student004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '赵六', 'zhaoliu@student.edu.cn', '13800000007', 'student', '信息安全', '2021001004', 1);

-- 插入示例课题
INSERT INTO sys_topic (title, description, teacher_id, teacher_name, major, difficulty, max_students, requirements, expected_outcome, deadline, status) VALUES
('基于深度学习的图像识别系统', '设计并实现一个基于深度学习的图像识别系统，能够识别多种物体和场景。', 2, '李教授', '计算机科学与技术', 'hard', 3, '熟悉Python编程,了解深度学习框架,有图像处理基础', '完成一个可用的图像识别系统，并撰写相关技术文档。', '2024-06-15 23:59:59', 'active'),
('校园二手交易平台设计与实现', '开发一个校园内的二手物品交易平台，支持用户发布、搜索、交易等功能。', 3, '王老师', '软件工程', 'medium', 2, '熟悉Web开发,了解数据库设计,有前端开发经验', '完成一个功能完整的Web应用系统。', '2024-06-20 23:59:59', 'active'),
('智能家居控制系统', '设计一个智能家居控制系统，能够远程控制家中的各种设备。', 4, '张教授', '网络工程', 'medium', 2, '熟悉物联网技术,了解嵌入式开发,有硬件基础', '完成一个可演示的智能家居控制系统原型。', '2024-06-25 23:59:59', 'active'),
('基于区块链的数据安全存储系统', '利用区块链技术设计一个安全的数据存储系统，确保数据的完整性和不可篡改性。', 2, '李教授', '信息安全', 'hard', 2, '了解区块链原理,熟悉密码学,有分布式系统基础', '完成一个基于区块链的数据存储系统原型。', '2024-06-30 23:59:59', 'active'),
('学生成绩管理系统', '开发一个学生成绩管理系统，支持成绩录入、查询、统计等功能。', 3, '王老师', '软件工程', 'easy', 4, '熟悉数据库操作,了解Web开发,有系统设计能力', '完成一个功能完整的管理系统。', '2024-07-01 23:59:59', 'active'),
('移动端健康管理应用', '开发一个移动端健康管理应用，帮助用户记录和管理健康数据。', 4, '张教授', '计算机科学与技术', 'medium', 3, '熟悉移动开发,了解健康数据管理,有UI设计能力', '完成一个可用的移动应用。', '2024-07-05 23:59:59', 'active');

-- 插入示例选题记录
INSERT INTO sys_selection (student_id, student_name, student_number, topic_id, topic_title, teacher_id, teacher_name, selection_time, status, progress) VALUES
(5, '张三', '2021001001', 1, '基于深度学习的图像识别系统', 2, '李教授', '2024-01-15 10:30:00', 'approved', 35),
(6, '李四', '2021001002', 2, '校园二手交易平台设计与实现', 3, '王老师', '2024-01-20 14:20:00', 'approved', 20),
(7, '王五', '2021001003', 3, '智能家居控制系统', 4, '张教授', '2024-01-25 09:15:00', 'pending', 0);

-- 插入示例进度记录
INSERT INTO sys_progress (selection_id, student_id, student_name, topic_id, percentage, description, milestone_title, milestone_description, milestone_status, milestone_date) VALUES
(1, 5, '张三', 1, 35, '已完成需求分析和技术调研，正在进行系统设计', '需求分析', '完成系统需求分析和功能设计', 'completed', '2024-01-20 00:00:00'),
(1, 5, '张三', 1, 35, '已完成需求分析和技术调研，正在进行系统设计', '技术调研', '调研相关技术和算法', 'completed', '2024-02-01 00:00:00'),
(1, 5, '张三', 1, 35, '已完成需求分析和技术调研，正在进行系统设计', '系统设计', '完成系统架构设计和数据库设计', 'current', '2024-02-15 00:00:00'),
(2, 6, '李四', 2, 20, '正在进行需求分析和系统设计', '需求分析', '完成系统需求分析和功能设计', 'completed', '2024-01-25 00:00:00'),
(2, 6, '李四', 2, 20, '正在进行需求分析和系统设计', '系统设计', '完成系统架构设计和数据库设计', 'current', '2024-02-10 00:00:00');

-- 插入示例文档
INSERT INTO sys_document (name, original_name, type, size, path, extension, uploader_id, uploader_name, selection_id, topic_id, status, download_count) VALUES
('需求分析文档.docx', '需求分析文档.docx', '需求文档', 2350000, '/uploads/documents/req_analysis_001.docx', 'docx', 5, '张三', 1, 1, 'approved', 3),
('系统设计文档.pdf', '系统设计文档.pdf', '设计文档', 5120000, '/uploads/documents/sys_design_001.pdf', 'pdf', 5, '张三', 1, 1, 'pending', 1),
('技术调研报告.docx', '技术调研报告.docx', '调研报告', 3200000, '/uploads/documents/tech_research_001.docx', 'docx', 5, '张三', 1, 1, 'approved', 2);

-- 插入示例系统日志
INSERT INTO sys_log (user_id, username, operation, description, method, params, time, ip, user_agent) VALUES
(5, 'student001', 'LOGIN', '用户登录系统', 'POST', '{"username":"student001"}', 150, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(5, 'student001', 'SELECT_TOPIC', '选择课题', 'POST', '{"topicId":1}', 200, '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(2, 'teacher001', 'PUBLISH_TOPIC', '发布课题', 'POST', '{"title":"基于深度学习的图像识别系统"}', 300, '192.168.1.101', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(1, 'admin', 'MANAGE_USER', '管理用户', 'GET', '{"page":1,"size":10}', 100, '192.168.1.102', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');

-- 创建视图：课题统计视图
CREATE VIEW v_topic_stats AS
SELECT 
    t.id,
    t.title,
    t.teacher_name,
    t.major,
    t.difficulty,
    t.max_students,
    t.selected_count,
    t.status,
    t.view_count,
    t.rating,
    CASE 
        WHEN t.selected_count >= t.max_students THEN '已满员'
        WHEN t.selected_count = 0 THEN '无人选择'
        ELSE CONCAT('还可选择', t.max_students - t.selected_count, '人')
    END AS availability_status
FROM sys_topic t
WHERE t.deleted = 0;

-- 创建视图：学生选题状态视图
CREATE VIEW v_student_selection_status AS
SELECT 
    u.id as student_id,
    u.username,
    u.real_name,
    u.student_id as student_number,
    u.department,
    s.id as selection_id,
    s.topic_title,
    s.teacher_name,
    s.status as selection_status,
    s.progress,
    s.selection_time,
    CASE 
        WHEN s.id IS NULL THEN '未选择'
        WHEN s.status = 'pending' THEN '待审核'
        WHEN s.status = 'approved' THEN '已通过'
        WHEN s.status = 'rejected' THEN '已拒绝'
        ELSE '未知状态'
    END AS status_text
FROM sys_user u
LEFT JOIN sys_selection s ON u.id = s.student_id AND s.deleted = 0
WHERE u.role = 'student' AND u.deleted = 0;

-- 创建存储过程：更新课题已选学生数
DELIMITER //
CREATE PROCEDURE UpdateTopicSelectedCount(IN topic_id BIGINT)
BEGIN
    DECLARE approved_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO approved_count
    FROM sys_selection 
    WHERE topic_id = topic_id AND status = 'approved' AND deleted = 0;
    
    UPDATE sys_topic 
    SET selected_count = approved_count 
    WHERE id = topic_id AND deleted = 0;
END //
DELIMITER ;

-- 创建触发器：选题状态变更时自动更新课题已选学生数
DELIMITER //
CREATE TRIGGER tr_selection_status_change
AFTER UPDATE ON sys_selection
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        CALL UpdateTopicSelectedCount(NEW.topic_id);
    END IF;
END //
DELIMITER ;

-- 创建触发器：删除选题记录时自动更新课题已选学生数
DELIMITER //
CREATE TRIGGER tr_selection_delete
AFTER UPDATE ON sys_selection
FOR EACH ROW
BEGIN
    IF NEW.deleted = 1 AND OLD.deleted = 0 THEN
        CALL UpdateTopicSelectedCount(NEW.topic_id);
    END IF;
END //
DELIMITER ;

-- 创建索引优化查询性能
CREATE INDEX idx_topic_title ON sys_topic(title);
CREATE INDEX idx_topic_create_time ON sys_topic(create_time);
CREATE INDEX idx_selection_create_time ON sys_selection(create_time);
CREATE INDEX idx_document_create_time ON sys_document(create_time);
CREATE INDEX idx_progress_create_time ON sys_progress(create_time);

-- 完成数据库初始化
SELECT 'Database initialization completed successfully!' AS message;
