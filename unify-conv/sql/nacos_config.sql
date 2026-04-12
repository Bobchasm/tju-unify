/*
 Navicat Premium Dump SQL

 Source Server         : TXCloud1
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : 101.42.109.48:3306
 Source Schema         : nacos_config

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 01/04/2026 23:16:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'group_id',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'configuration description',
  `c_use` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'configuration usage',
  `effect` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '配置生效的描述',
  `type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '配置的类型',
  `c_schema` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT '配置的模式',
  `encrypted_data_key` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT '密钥',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfo_datagrouptenant`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info` VALUES (1, 'ai-application.yaml', 'DEFAULT_GROUP', 'deepseek:\r\n  base-url: https://api.deepseek.com\r\n  chat-endpoint: /chat/completions\r\n  model: deepseek-chat\r\n  max-tokens: 1024\r\n  temperature: 0.7\r\n  top-p: 0.9\r\n  timeout-seconds: 30\r\n  max-retries: 3', '37da3b32046853001aa077a3e98fc08a', '2026-04-01 15:10:47', '2026-04-01 15:10:47', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (2, 'shared-log.yaml', 'DEFAULT_GROUP', 'logging:\r\n  level:\r\n    com.tju.elm: debug\r\n  pattern:\r\n    dateformat: HH:mm:ss:SSS\r\n  file:\r\n    path: \"logs/${spring.application.name}\"', 'c97fbebc59eccd53898fabc2e08f5dc3', '2026-04-01 15:11:07', '2026-04-01 15:11:07', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (3, 'shared-mybatis.yaml', 'DEFAULT_GROUP', 'spring:\r\n  datasource:\r\n    driver-class-name: com.mysql.cj.jdbc.Driver\r\n    url: jdbc:mysql://${elm.db.host:101.42.109.48}:3306/${elm.db.database}\r\n    username: ${elm.db.username:root}\r\n    password: ${elm.db.pw:Elm@2026Secure!}\r\n    hikari:\r\n      transaction-isolation: TRANSACTION_READ_COMMITTED\r\n\r\nmybatis:\r\n  configuration:\r\n    map-underscore-to-camel-case: true\r\n  mapper-locations: classpath:${elm.mybatis.mp:/mapper/*.xml}', '1a0402c9339352f64b95635d5a3bc9a8', '2026-04-01 15:11:58', '2026-04-01 15:11:58', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (4, 'shared-nacos.yaml', 'DEFAULT_GROUP', 'spring:\r\n  cloud:\r\n    sentinel:\r\n      enabled: true\r\n      transport:\r\n        dashboard: ${elm.nacos.server-addr:101.42.109.48}:8090\r\n        port: 8099\r\n        heartbeat-interval-ms: 5000\r\n      eager: true\r\n      http-method-specify: true\r\n    nacos:\r\n      discovery:\r\n        server-addr: ${elm.nacos.server-addr:101.42.109.48}\r\n        namespace: public\r\n        group: DEFAULT_GROUP\r\n        ip: ${elm.nacos.server:127.0.0.1}\r\n        port: ${server.port}\r\n        enabled: true\r\n        register-enabled: true\r\n        metadata:\r\n          version: 1.0\r\n        heartbeat:\r\n          interval: 5000  # 心跳间隔（毫秒）', '78e07b913c74dbdc75fdc463dbc8bc6e', '2026-04-01 15:12:19', '2026-04-01 15:12:19', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (5, 'shared-rabbitmq.yaml', 'DEFAULT_GROUP', '  rabbitmq:\r\n    host: ${elm.rabbitmq.host:101.42.109.48}\r\n    port: 5672\r\n    username: ${elm.rabbitmq.username:rabbit}\r\n    password: ${elm.rabbitmq.password:rabbit}\r\n    virtual-host: /', 'f479201c71a3fabf4d9c60bb069b4450', '2026-04-01 15:12:37', '2026-04-01 15:12:37', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (6, 'shared-redis.yaml', 'DEFAULT_GROUP', 'spring:\r\n  data:\r\n    redis:\r\n      host: ${elm.data.redis.host:101.42.109.48}\r\n      port: 6379\r\n      password: ${elm.redis.pw:}\r\n      database: ${elm.redis.db}\r\n      timeout: 3000ms\r\n      lettuce:\r\n        pool:\r\n          max-active: 8\r\n          max-idle: 8\r\n          min-idle: 0\r\n          max-wait: -1ms', '961970d41df7ef9cbd4b2781b896581d', '2026-04-01 15:12:53', '2026-04-01 15:12:53', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (7, 'shared-swagger.yaml', 'DEFAULT_GROUP', 'shared-swagger.yamlspringdoc:\r\n  api-docs:\r\n    enabled: true\r\n    path: /v3/api-docs\r\n  swagger-ui:\r\n    enabled: true\r\n    path: /swagger-ui', '90a33e06cd639c607090a790c233b997', '2026-04-01 15:13:15', '2026-04-01 15:13:15', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info` VALUES (8, 'user-service.yaml', 'DEFAULT_GROUP', 'jwt:\r\n  token-validity-in-seconds: 86400\r\n  token-validity-in-seconds-for-remember-me: 1080000', '04b970c6d2131ddec19fff1901897586', '2026-04-01 15:13:31', '2026-04-01 15:13:31', 'nacos', '202.113.184.139', '', '', NULL, NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- Table structure for config_info_gray
-- ----------------------------
DROP TABLE IF EXISTS `config_info_gray`;
CREATE TABLE `config_info_gray`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'group_id',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'md5',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT 'src_user',
  `src_ip` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'src_ip',
  `gmt_create` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'gmt_create',
  `gmt_modified` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT 'gmt_modified',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'app_name',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '' COMMENT 'tenant_id',
  `gray_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'gray_name',
  `gray_rule` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'gray_rule',
  `encrypted_data_key` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '' COMMENT 'encrypted_data_key',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_configinfogray_datagrouptenantgray`(`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `gray_name` ASC) USING BTREE,
  INDEX `idx_dataid_gmt_modified`(`data_id` ASC, `gmt_modified` ASC) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = 'config_info_gray' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_info_gray
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`  (
  `id` bigint NOT NULL COMMENT 'id',
  `tag_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_name',
  `tag_type` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'tag_type',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `nid` bigint NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增长标识',
  PRIMARY KEY (`nid`) USING BTREE,
  UNIQUE INDEX `uk_configtagrelation_configidtag`(`id` ASC, `tag_name` ASC, `tag_type` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'config_tag_relation' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '集群、各Group容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------
INSERT INTO `group_capacity` VALUES (1, '', 0, 8, 0, 0, 0, 0, '2026-04-01 23:10:47', '2026-04-01 23:13:31');
INSERT INTO `group_capacity` VALUES (2, 'DEFAULT_GROUP', 0, 8, 0, 0, 0, 0, '2026-04-01 23:10:47', '2026-04-01 23:13:31');

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`  (
  `id` bigint UNSIGNED NOT NULL COMMENT 'id',
  `nid` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'nid, 自增标识',
  `data_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
  `group_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
  `app_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
  `content` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'content',
  `md5` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'source user',
  `src_ip` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'source ip',
  `op_type` char(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'operation type',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
  `encrypted_data_key` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT '密钥',
  `publish_type` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT 'formal' COMMENT 'publish type gray or formal',
  `gray_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'gray name',
  `ext_info` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL COMMENT 'ext info',
  PRIMARY KEY (`nid`) USING BTREE,
  INDEX `idx_gmt_create`(`gmt_create` ASC) USING BTREE,
  INDEX `idx_gmt_modified`(`gmt_modified` ASC) USING BTREE,
  INDEX `idx_did`(`data_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '多租户改造' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info` VALUES (0, 1, 'ai-application.yaml', 'DEFAULT_GROUP', '', 'deepseek:\r\n  base-url: https://api.deepseek.com\r\n  chat-endpoint: /chat/completions\r\n  model: deepseek-chat\r\n  max-tokens: 1024\r\n  temperature: 0.7\r\n  top-p: 0.9\r\n  timeout-seconds: 30\r\n  max-retries: 3', '37da3b32046853001aa077a3e98fc08a', '2026-04-01 15:10:47', '2026-04-01 23:10:47', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 2, 'shared-log.yaml', 'DEFAULT_GROUP', '', 'logging:\r\n  level:\r\n    com.tju.elm: debug\r\n  pattern:\r\n    dateformat: HH:mm:ss:SSS\r\n  file:\r\n    path: \"logs/${spring.application.name}\"', 'c97fbebc59eccd53898fabc2e08f5dc3', '2026-04-01 15:11:06', '2026-04-01 23:11:07', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 3, 'shared-mybatis.yaml', 'DEFAULT_GROUP', '', 'spring:\r\n  datasource:\r\n    driver-class-name: com.mysql.cj.jdbc.Driver\r\n    url: jdbc:mysql://${elm.db.host:101.42.109.48}:3306/${elm.db.database}\r\n    username: ${elm.db.username:root}\r\n    password: ${elm.db.pw:Elm@2026Secure!}\r\n    hikari:\r\n      transaction-isolation: TRANSACTION_READ_COMMITTED\r\n\r\nmybatis:\r\n  configuration:\r\n    map-underscore-to-camel-case: true\r\n  mapper-locations: classpath:${elm.mybatis.mp:/mapper/*.xml}', '1a0402c9339352f64b95635d5a3bc9a8', '2026-04-01 15:11:58', '2026-04-01 23:11:58', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 4, 'shared-nacos.yaml', 'DEFAULT_GROUP', '', 'spring:\r\n  cloud:\r\n    sentinel:\r\n      enabled: true\r\n      transport:\r\n        dashboard: ${elm.nacos.server-addr:101.42.109.48}:8090\r\n        port: 8099\r\n        heartbeat-interval-ms: 5000\r\n      eager: true\r\n      http-method-specify: true\r\n    nacos:\r\n      discovery:\r\n        server-addr: ${elm.nacos.server-addr:101.42.109.48}\r\n        namespace: public\r\n        group: DEFAULT_GROUP\r\n        ip: ${elm.nacos.server:127.0.0.1}\r\n        port: ${server.port}\r\n        enabled: true\r\n        register-enabled: true\r\n        metadata:\r\n          version: 1.0\r\n        heartbeat:\r\n          interval: 5000  # 心跳间隔（毫秒）', '78e07b913c74dbdc75fdc463dbc8bc6e', '2026-04-01 15:12:18', '2026-04-01 23:12:19', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 5, 'shared-rabbitmq.yaml', 'DEFAULT_GROUP', '', '  rabbitmq:\r\n    host: ${elm.rabbitmq.host:101.42.109.48}\r\n    port: 5672\r\n    username: ${elm.rabbitmq.username:rabbit}\r\n    password: ${elm.rabbitmq.password:rabbit}\r\n    virtual-host: /', 'f479201c71a3fabf4d9c60bb069b4450', '2026-04-01 15:12:36', '2026-04-01 23:12:37', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 6, 'shared-redis.yaml', 'DEFAULT_GROUP', '', 'spring:\r\n  data:\r\n    redis:\r\n      host: ${elm.data.redis.host:101.42.109.48}\r\n      port: 6379\r\n      password: ${elm.redis.pw:}\r\n      database: ${elm.redis.db}\r\n      timeout: 3000ms\r\n      lettuce:\r\n        pool:\r\n          max-active: 8\r\n          max-idle: 8\r\n          min-idle: 0\r\n          max-wait: -1ms', '961970d41df7ef9cbd4b2781b896581d', '2026-04-01 15:12:52', '2026-04-01 23:12:53', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 7, 'shared-swagger.yaml', 'DEFAULT_GROUP', '', 'shared-swagger.yamlspringdoc:\r\n  api-docs:\r\n    enabled: true\r\n    path: /v3/api-docs\r\n  swagger-ui:\r\n    enabled: true\r\n    path: /swagger-ui', '90a33e06cd639c607090a790c233b997', '2026-04-01 15:13:15', '2026-04-01 23:13:15', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');
INSERT INTO `his_config_info` VALUES (0, 8, 'user-service.yaml', 'DEFAULT_GROUP', '', 'jwt:\r\n  token-validity-in-seconds: 86400\r\n  token-validity-in-seconds-for-remember-me: 1080000', '04b970c6d2131ddec19fff1901897586', '2026-04-01 15:13:30', '2026-04-01 23:13:31', 'nacos', '202.113.184.139', 'I', '', '', 'formal', '', '{\"src_user\":\"nacos\",\"type\":\"yaml\"}');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'role',
  `resource` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'resource',
  `action` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'action',
  UNIQUE INDEX `uk_role_permission`(`role` ASC, `resource` ASC, `action` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'username',
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'role',
  UNIQUE INDEX `idx_user_role`(`username` ASC, `role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
  `quota` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
  `usage` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用量',
  `max_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
  `max_aggr_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
  `max_aggr_size` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
  `max_history_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '租户容量信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `kp` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'kp',
  `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
  `tenant_name` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_name',
  `tenant_desc` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
  `create_source` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'create_source',
  `gmt_create` bigint NOT NULL COMMENT '创建时间',
  `gmt_modified` bigint NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_info_kptenantid`(`kp` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = 'tenant_info' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'username',
  `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'password',
  `enabled` tinyint(1) NOT NULL COMMENT 'enabled',
  PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('nacos', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 1);

SET FOREIGN_KEY_CHECKS = 1;
