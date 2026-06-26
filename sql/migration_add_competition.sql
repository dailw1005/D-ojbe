-- ============================================================
-- 比赛模块增量迁移脚本
-- 用途：已有数据库添加比赛模块所需的新表和字段
-- 执行方式：mysql -u root -p dailw < migration_add_competition.sql
-- 注意：所有操作使用 IF NOT EXISTS / IF EXISTS，可重复执行
-- ============================================================

USE `dailw`;

-- 1. 给 question_submit 表增加 competition_id 字段（使用存储过程兼容低版本 MySQL）
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS add_competition_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'dailw'
          AND TABLE_NAME = 'question_submit'
          AND COLUMN_NAME = 'competition_id'
    ) THEN
        ALTER TABLE `question_submit`
            ADD COLUMN `competition_id` BIGINT DEFAULT NULL COMMENT '比赛ID（NULL表示非比赛提交）' AFTER `user_id`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = 'dailw'
          AND TABLE_NAME = 'question_submit'
          AND INDEX_NAME = 'idx_competition_id'
    ) THEN
        ALTER TABLE `question_submit`
            ADD INDEX `idx_competition_id` (`competition_id`);
    END IF;
END$$

DELIMITER ;

CALL add_competition_column();
DROP PROCEDURE IF EXISTS add_competition_column;

-- 2. 比赛表
CREATE TABLE IF NOT EXISTS `competition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '比赛ID',
    `title` VARCHAR(200) NOT NULL COMMENT '比赛标题',
    `description` TEXT DEFAULT NULL COMMENT '比赛说明（Markdown格式）',
    `type` VARCHAR(10) NOT NULL COMMENT '评分模式：ACM（解题数+罚时）- OI（总分）',
    `start_time` DATETIME NOT NULL COMMENT '比赛开始时间',
    `end_time` DATETIME NOT NULL COMMENT '比赛结束时间',
    `password` VARCHAR(100) DEFAULT NULL COMMENT '参赛密码（NULL表示公开比赛，无需密码）',
    `user_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '逻辑删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_end_time` (`end_time`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛信息表';

-- 3. 比赛-题目关联表
CREATE TABLE IF NOT EXISTS `competition_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `competition_id` BIGINT NOT NULL COMMENT '比赛ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `display_order` INT DEFAULT 0 COMMENT '题目序号（用于展示排序）',
    `score` INT DEFAULT NULL COMMENT 'OI模式下的题目分值（ACM模式可为NULL）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_competition_question` (`competition_id`, `question_id`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛题目关联表';

-- 4. 比赛报名表
CREATE TABLE IF NOT EXISTS `competition_registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    `competition_id` BIGINT NOT NULL COMMENT '比赛ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '报名时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_competition_user` (`competition_id`, `user_id`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛报名表';
