-- ============================================================
-- 用户声望与等级系统迁移脚本
-- 用途：给 user 表增加 reputation 和 level 字段
-- 执行方式：mysql -u root -p dailw < migration_add_reputation.sql
-- ============================================================

USE `dailw`;

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS add_reputation_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'dailw'
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'reputation'
    ) THEN
        ALTER TABLE `user`
            ADD COLUMN `reputation` INT DEFAULT 0 COMMENT '声望值' AFTER `role`;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'dailw'
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'level'
    ) THEN
        ALTER TABLE `user`
            ADD COLUMN `level` TINYINT DEFAULT 0 COMMENT '用户等级：0-9' AFTER `reputation`;
    END IF;
END$$

DELIMITER ;

CALL add_reputation_columns();
DROP PROCEDURE IF EXISTS add_reputation_columns;
