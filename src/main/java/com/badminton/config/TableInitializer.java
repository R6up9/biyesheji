package com.badminton.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TableInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========== 开始数据库初始化 ==========");

        // 1. 检查 b_product_order 表
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM b_product_order LIMIT 1", Integer.class);
            System.out.println("✅ b_product_order 表已存在，无需创建");
        } catch (Exception e) {
            System.out.println("🔄 正在创建 b_product_order 表...");
            String createTableSql = "CREATE TABLE IF NOT EXISTS `b_product_order` (" +
                    "`id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID'," +
                    "`order_no` varchar(50) NOT NULL COMMENT '订单号'," +
                    "`user_id` int(11) NOT NULL COMMENT '用户ID'," +
                    "`user_name` varchar(50) DEFAULT NULL COMMENT '用户姓名'," +
                    "`product_id` int(11) NOT NULL COMMENT '商品ID'," +
                    "`product_name` varchar(100) NOT NULL COMMENT '商品名称'," +
                    "`product_image` varchar(500) DEFAULT NULL COMMENT '商品图片'," +
                    "`quantity` int(11) NOT NULL COMMENT '购买数量'," +
                    "`price` decimal(10,2) NOT NULL COMMENT '单价'," +
                    "`total_amount` decimal(10,2) NOT NULL COMMENT '总金额'," +
                    "`status` int(11) NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款, 1-已付款, 2-已发货, 3-已完成, 4-已取消'," +
                    "`address` varchar(500) DEFAULT NULL COMMENT '收货地址'," +
                    "`phone` varchar(20) DEFAULT NULL COMMENT '联系电话'," +
                    "`receiver` varchar(50) DEFAULT NULL COMMENT '收货人'," +
                    "`remark` varchar(500) DEFAULT NULL COMMENT '备注'," +
                    "`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "`update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                    "PRIMARY KEY (`id`)," +
                    "KEY `idx_user_id` (`user_id`)," +
                    "KEY `idx_order_no` (`order_no`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单表'";
            jdbcTemplate.execute(createTableSql);
            System.out.println("✅ b_product_order 表创建成功！");
        }

        // 2. 检查并添加 b_activity 表的 price 字段
        try {
            jdbcTemplate.queryForObject("SELECT price FROM b_activity LIMIT 1", Object.class);
            System.out.println("✅ b_activity.price 字段已存在，无需添加");
        } catch (Exception e) {
            System.out.println("🔄 正在添加 b_activity.price 字段...");
            try {
                String addColumnSql = "ALTER TABLE b_activity ADD COLUMN price DECIMAL(10,2) DEFAULT NULL COMMENT '活动价格' AFTER max_participants";
                jdbcTemplate.execute(addColumnSql);
                System.out.println("✅ b_activity.price 字段添加成功！");
            } catch (Exception e2) {
                System.out.println("⚠️ 添加字段可能已存在，跳过：" + e2.getMessage());
            }
        }

        // 3. 检查并添加 b_booking 表的 amount 字段
        try {
            jdbcTemplate.queryForObject("SELECT amount FROM b_booking LIMIT 1", Object.class);
            System.out.println("✅ b_booking.amount 字段已存在，无需添加");
        } catch (Exception e) {
            System.out.println("🔄 正在添加 b_booking.amount 字段...");
            try {
                String addColumnSql = "ALTER TABLE b_booking ADD COLUMN amount DECIMAL(10,2) DEFAULT NULL COMMENT '预约金额' AFTER status";
                jdbcTemplate.execute(addColumnSql);
                System.out.println("✅ b_booking.amount 字段添加成功！");
            } catch (Exception e2) {
                System.out.println("⚠️ 添加字段可能已存在，跳过：" + e2.getMessage());
            }
        }

        // 4. 为会员设置默认余额（方便测试）
        try {
            jdbcTemplate.update("UPDATE b_member SET balance = 500.00 WHERE balance IS NULL OR balance = 0");
            System.out.println("✅ 已为会员设置默认余额！");
        } catch (Exception e) {
            System.out.println("⚠️ 设置默认余额时出错：" + e.getMessage());
        }

        System.out.println("========== 数据库初始化完成 ==========");
    }
}
