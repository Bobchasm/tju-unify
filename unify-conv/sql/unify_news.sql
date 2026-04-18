CREATE TABLE IF NOT EXISTS `school_news` (
  `id`       VARCHAR(64)  NOT NULL COMMENT '主键UUID',
  `content`  LONGTEXT     NULL COMMENT '新闻正文',
  `time`     VARCHAR(64)  NULL COMMENT '新闻发布时间原文',
  `title`    VARCHAR(512) NULL COMMENT '标题',
  `unit`     VARCHAR(128) NULL COMMENT '发布单位',
  `flag`     BIGINT       NULL COMMENT '栏目标识',
  `origin`   VARCHAR(255) NULL COMMENT '来源',
  `url`      VARCHAR(1024) NULL COMMENT '新闻原始链接',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_news_url` (`url`(255)),
  KEY `idx_school_news_flag_id` (`flag`, `id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='学校新闻表';