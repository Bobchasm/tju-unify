-- 为post表添加images列
ALTER TABLE `post` ADD COLUMN `images` TEXT NULL COMMENT '图片URL，多个用逗号分隔' AFTER `price`;