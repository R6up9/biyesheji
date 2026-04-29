-- =====================================================
-- 羽毛球俱乐部管理系统 - 数据库更新脚本
-- 功能：添加支付相关字段
-- =====================================================

-- 1. 为 b_activity 表添加 price 字段
ALTER TABLE b_activity
ADD COLUMN IF NOT EXISTS price DECIMAL(10, 2) DEFAULT NULL COMMENT '活动价格'
AFTER max_participants;

-- 2. 为 b_booking 表添加 amount 字段
ALTER TABLE b_booking
ADD COLUMN IF NOT EXISTS amount DECIMAL(10, 2) DEFAULT NULL COMMENT '预约金额'
AFTER status;

-- 3. 为已有的会员设置一个默认余额（方便测试）
-- 你可以根据实际情况修改或删除这部分
UPDATE b_member
SET balance = 500.00
WHERE balance IS NULL;

SELECT '数据库更新完成！' AS message;
