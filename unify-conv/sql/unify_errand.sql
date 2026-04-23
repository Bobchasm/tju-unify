-- 校园跑腿：请在 MySQL 中执行本文件（可先建库再执行表结构，或整文件执行）。
-- 建议库名：unify_errand（与 conv-errand 服务 datasource 一致）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `unify_errand` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `unify_errand`;

-- ----------------------------
-- 跑腿订单
-- ----------------------------
DROP TABLE IF EXISTS `conv_errand_order`;
CREATE TABLE `conv_errand_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `publisher_id` bigint NOT NULL COMMENT '发起人（电商用户ID）',
  `start_point` varchar(512) NOT NULL COMMENT '起点',
  `end_point` varchar(512) NOT NULL COMMENT '终点',
  `tip_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '小费（元）',
  `expect_time` datetime NOT NULL COMMENT '期望送达/取件时间',
  `contact_info` varchar(255) NOT NULL COMMENT '联系方式',
  `remark` text NULL COMMENT '补充说明（可选）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-待接单 1-已接单 2-已完成 3-已取消',
  `runner_id` bigint NULL DEFAULT NULL COMMENT '接单者用户ID',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_errand_status_time` (`status`, `expect_time`, `is_deleted`),
  KEY `idx_errand_publisher` (`publisher_id`, `is_deleted`),
  KEY `idx_errand_runner` (`runner_id`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园跑腿订单';

SET FOREIGN_KEY_CHECKS = 1;
