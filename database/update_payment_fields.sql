-- 更新数据库，添加支付相关字段
-- 执行时间：2026-04-29

-- 1. 为 b_activity 表添加 price 字段
ALTER TABLE b_activity
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) DEFAULT NULL COMMENT '活动价格'
AFTER max_participants;

-- 2. 为 b_booking 表添加 amount 字段
ALTER TABLE b_booking
ADD COLUMN IF NOT EXISTS amount DECIMAL(10, 2) DEFAULT NULL COMMENT '预约金额'
AFTER status;

-- 3. 初始化会员余额（方便测试）
UPDATE b_member
SET balance = 500.00
WHERE balance IS NULL OR balance = 0;

-- 4. 可选：给已有数据设置示例价格
UPDATE b_course
SET price = 200.00
WHERE price IS NULL;

UPDATE b_activity
SET price = 150.00
WHERE price IS NULL;

SELECT '数据库更新完成！' AS message;
