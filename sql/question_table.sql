-- 题目表
CREATE TABLE `question` (
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
) COMMENT='题目' COLLATE=utf8mb4_unicode_ci;

-- 题目提交表
CREATE TABLE `question_submit` (
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
) COMMENT='题目提交' COLLATE=utf8mb4_unicode_ci;

-- 题目代码模板表 (扩展)
CREATE TABLE `question_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `language` VARCHAR(64) NOT NULL COMMENT '编程语言',
    `code` TEXT NOT NULL COMMENT '模板代码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`)
) COMMENT='题目代码模板' COLLATE=utf8mb4_unicode_ci;

-- 题解表 (扩展)
CREATE TABLE `question_solution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `user_id` BIGINT NOT NULL COMMENT '发布用户 id',
    `title` VARCHAR(512) DEFAULT NULL COMMENT '题解标题',
    `content` TEXT DEFAULT NULL COMMENT '题解内容',
    `tags` VARCHAR(1024) DEFAULT NULL COMMENT '题解标签（json数组）',
    `thumb_num` INT DEFAULT 0 NOT NULL COMMENT '点赞数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_user_id` (`user_id`)
) COMMENT='题解' COLLATE=utf8mb4_unicode_ci;

-- 标签表 (扩展)
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
    `user_id` BIGINT NOT NULL COMMENT '创建用户 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uni_idx_name` (`name`),
    INDEX `idx_user_id` (`user_id`)
) COMMENT='标签' COLLATE=utf8mb4_unicode_ci;

-- 题目标签关联表 (扩展)
CREATE TABLE `question_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'id',
    `question_id` BIGINT NOT NULL COMMENT '题目 id',
    `tag_id` BIGINT NOT NULL COMMENT '标签 id',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_tag_id` (`tag_id`)
) COMMENT='题目标签关联' COLLATE=utf8mb4_unicode_ci;
