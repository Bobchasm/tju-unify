/*
  演示用种子数据（库：unify_memo）
  将 @USER_ID 改为你在电商侧的用户数字 ID（与 Swagger 里填的 username 对应 getUserByName 的 id 一致）。
  适合在空表或测试环境执行；若已有自增数据，可先 TRUNCATE 三张表（慎用）或改 id。
*/

SET NAMES utf8mb4;

SET @USER_ID := 7;

INSERT INTO `conv_memo_category` (`user_id`, `name`, `sort_order`, `is_deleted`)
VALUES (@USER_ID, '工作', 0, 0);
SET @CAT1 := LAST_INSERT_ID();

INSERT INTO `conv_memo_category` (`user_id`, `name`, `sort_order`, `is_deleted`)
VALUES (@USER_ID, '生活', 10, 0);
SET @CAT2 := LAST_INSERT_ID();

INSERT INTO `conv_memo` (`user_id`, `category_id`, `title`, `content`, `pinned`, `sort_order`, `remind_at`, `remind_done`, `is_deleted`)
VALUES (@USER_ID, @CAT1, '本周事项', '记得跟进联调与文档', 1, 0, DATE_ADD(NOW(), INTERVAL 1 DAY), 0, 0);
SET @MEMO1 := LAST_INSERT_ID();

INSERT INTO `conv_memo` (`user_id`, `category_id`, `title`, `content`, `pinned`, `sort_order`, `remind_at`, `remind_done`, `is_deleted`)
VALUES (@USER_ID, @CAT2, '购物清单', '牛奶、鸡蛋', 0, 0, NULL, 0, 0);
SET @MEMO2 := LAST_INSERT_ID();

INSERT INTO `conv_memo_task` (`memo_id`, `title`, `done`, `sort_order`, `is_deleted`)
VALUES (@MEMO1, '联调备忘录接口', 0, 0, 0),
       (@MEMO1, '写前端页面', 1, 1, 0),
       (@MEMO2, '买牛奶', 0, 0, 0);
