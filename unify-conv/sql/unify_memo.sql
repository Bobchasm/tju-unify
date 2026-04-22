

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 备忘录分类
-- ----------------------------
DROP TABLE IF EXISTS `conv_memo_category`;
CREATE TABLE `conv_memo_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户（电商用户ID）',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_conv_memo_category_user` (`user_id`, `is_deleted`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录分类';

-- ----------------------------
-- 备忘录主表
-- ----------------------------
DROP TABLE IF EXISTS `conv_memo_task`;
DROP TABLE IF EXISTS `conv_memo`;
CREATE TABLE `conv_memo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID，NULL 表示未分类',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` text NULL COMMENT '正文',
  `pinned` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '同分类/列表内排序，越小越靠前',
  `remind_at` datetime NULL DEFAULT NULL COMMENT '定时提醒时间，NULL 表示不提醒',
  `remind_done` tinyint NOT NULL DEFAULT 0 COMMENT '提醒是否已处理 0-待处理 1-已处理/忽略',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_conv_memo_user_cat` (`user_id`, `category_id`, `is_deleted`, `pinned`, `sort_order`),
  KEY `idx_conv_memo_remind` (`user_id`, `is_deleted`, `remind_done`, `remind_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录';

-- ----------------------------
-- 备忘录子任务
-- ----------------------------
CREATE TABLE `conv_memo_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `memo_id` bigint NOT NULL COMMENT '所属备忘录',
  `title` varchar(512) NOT NULL COMMENT '子任务标题',
  `done` tinyint NOT NULL DEFAULT 0 COMMENT '是否完成 0-否 1-是',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_conv_memo_task_memo` (`memo_id`, `is_deleted`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备忘录子任务';

SET FOREIGN_KEY_CHECKS = 1;
