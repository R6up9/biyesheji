-- 教练表
CREATE TABLE IF NOT EXISTS b_coach (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    real_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    gender INT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '联系电话',
    avatar VARCHAR(500) COMMENT '头像URL',
    introduction TEXT COMMENT '简介',
    skills TEXT COMMENT '特长/资质',
    hourly_rate DECIMAL(10, 2) DEFAULT 0 COMMENT '课时费',
    status INT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';
