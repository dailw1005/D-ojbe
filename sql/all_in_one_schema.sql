-- ============================================================
-- dailw-oj 数据库汇总脚本（整合版）
-- 来源：
-- 1) user_table.sql
-- 2) question_table.sql
-- 3) temp_add_view_num_to_question_solution.sql
-- ============================================================

-- ============================================================
-- 零、建库与基础设置
-- ============================================================

CREATE DATABASE IF NOT EXISTS `dailw`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE `dailw`;

SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 一、用户模块
-- ============================================================

-- 用户表结构
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `email` VARCHAR(100) COMMENT '邮箱地址',
    `phone` VARCHAR(20) COMMENT '手机号码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE COMMENT '生日',
    `status` TINYINT DEFAULT 1 COMMENT '账户状态：0-禁用，1-正常，2-锁定',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) COMMENT '最后登录IP',
    `login_count` INT DEFAULT 0 COMMENT '登录次数',
    `remark` VARCHAR(500) COMMENT '备注信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标志：0-未删除，1-已删除',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '用户角色：admin-管理员，user-普通用户，guest-访客',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 插入示例数据
INSERT IGNORE INTO `user` (`username`, `password`, `email`, `phone`, `nickname`, `gender`, `status`, `role`, `create_by`) VALUES
('admin', '$2a$10$7JB720yubVSOfvVWdBYoOeymw.HR6CTKqjjU.qFUleOqahZLjUoSS', 'admin@example.com', '13800138000', '管理员', 1, 1, 'admin', 1),
('user001', '$2a$10$7JB720yubVSOfvVWdBYoOeymw.HR6CTKqjjU.qFUleOqahZLjUoSS', 'user001@example.com', '13800138001', '普通用户', 2, 1, 'user', 1);

-- ============================================================
-- 二、题目与判题模块
-- ============================================================

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `title` VARCHAR(512) DEFAULT NULL COMMENT '标题',
    `content` TEXT DEFAULT NULL COMMENT '内容',
    `tags` VARCHAR(1024) DEFAULT NULL COMMENT '标签列表（json数组）',
    `answer` TEXT DEFAULT NULL COMMENT '题目答案',
    `submit_num` INT DEFAULT 0 NOT NULL COMMENT '提交数',
    `accepted_num` INT DEFAULT 0 NOT NULL COMMENT '通过数',
    `judge_case` TEXT DEFAULT NULL COMMENT '判题用例（json数组）',
    `judge_config` TEXT DEFAULT NULL COMMENT '判题配置（json对象）',
    `thumb_num` INT DEFAULT 0 NOT NULL COMMENT '点赞数',
    `favour_num` INT DEFAULT 0 NOT NULL COMMENT '收藏数',
    `difficulty` VARCHAR(64) DEFAULT NULL COMMENT '难度',
    `user_id` BIGINT NOT NULL COMMENT '创建用户 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目';

-- 题目提交表
CREATE TABLE IF NOT EXISTS `question_submit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `language` VARCHAR(128) NOT NULL COMMENT '编程语言',
    `code` TEXT NOT NULL COMMENT '用户代码',
    `judge_info` TEXT DEFAULT NULL COMMENT '判题信息（json对象）',
    `status` INT DEFAULT 0 NOT NULL COMMENT '判题状态（0-待判题、1-判题中、2-成功、3-失败）',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `user_id` BIGINT NOT NULL COMMENT '创建用户 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目提交';

-- 题目代码模板表 (扩展)
CREATE TABLE IF NOT EXISTS `question_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `language` VARCHAR(64) NOT NULL COMMENT '编程语言',
    `code` TEXT NOT NULL COMMENT '模板代码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目代码模板';

-- 题解表 (扩展)
CREATE TABLE IF NOT EXISTS `question_solution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `user_id` BIGINT NOT NULL COMMENT '发布用户 id',
    `title` VARCHAR(512) DEFAULT NULL COMMENT '题解标题',
    `content` TEXT DEFAULT NULL COMMENT '题解内容',
    `tags` VARCHAR(1024) DEFAULT NULL COMMENT '题解标签（json数组）',
    `thumb_num` INT DEFAULT 0 NOT NULL COMMENT '点赞数',
    `view_num` INT DEFAULT 0 NOT NULL COMMENT '浏览量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题解';

-- 标签表 (扩展)
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `user_id` BIGINT NOT NULL COMMENT '创建用户 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_idx_name` (`name`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签';

-- 题目标签关联表 (扩展)
CREATE TABLE IF NOT EXISTS `question_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `tag_id` BIGINT NOT NULL COMMENT '标签 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联';
