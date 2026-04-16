-- 已有库增量：进度链路节点材料提交表
USE thesis_connect;

CREATE TABLE IF NOT EXISTS sys_selection_node_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    selection_id BIGINT NOT NULL COMMENT '选题ID',
    node_index INT NOT NULL COMMENT '链路节点序号 0..n-1',
    description TEXT COMMENT '阶段说明',
    report_url VARCHAR(500) COMMENT '材料链接或文档地址',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending-待审核 approved-已通过 rejected-已驳回',
    reject_reason TEXT COMMENT '驳回原因',
    reviewer_id BIGINT COMMENT '审核人',
    reviewer_name VARCHAR(50) COMMENT '审核人姓名',
    review_time DATETIME COMMENT '审核时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_sel (selection_id),
    INDEX idx_sel_node (selection_id, node_index),
    FOREIGN KEY (selection_id) REFERENCES sys_selection(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进度链路节点材料';
