-- ============================================================
-- Badminton Club Management System - Database Creation Script
-- Database name: maoqiu_gsx
-- ============================================================

CREATE DATABASE IF NOT EXISTS maoqiu_gsx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE maoqiu_gsx;

-- 1. User account table
DROP TABLE IF EXISTS `b_user`;
CREATE TABLE `b_user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(200) NOT NULL,
  `role` TINYINT NOT NULL DEFAULT 1 COMMENT '0-admin, 1-member, 2-coach',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled, 1-active',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (`username`),
  INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Member/Coach profile table
DROP TABLE IF EXISTS `b_member`;
CREATE TABLE `b_member` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL UNIQUE,
  `real_name` VARCHAR(50),
  `gender` TINYINT DEFAULT 0 COMMENT '0-unknown, 1-male, 2-female',
  `age` INT,
  `phone` VARCHAR(20),
  `avatar` VARCHAR(255),
  `introduction` TEXT,
  `level` TINYINT DEFAULT 1 COMMENT 'level 1-5',
  `points` INT DEFAULT 0,
  `balance` DECIMAL(10,2) DEFAULT 0.00,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Court table
DROP TABLE IF EXISTS `b_court`;
CREATE TABLE `b_court` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `location` VARCHAR(200),
  `capacity` INT DEFAULT 10,
  `status` TINYINT DEFAULT 1 COMMENT '0-maintenance, 1-available',
  `price_per_hour` DECIMAL(8,2) DEFAULT 50.00,
  `description` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Booking table
DROP TABLE IF EXISTS `b_booking`;
CREATE TABLE `b_booking` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `target_id` INT NOT NULL COMMENT 'court/course/coach id',
  `type` TINYINT NOT NULL COMMENT '1-court, 2-coach, 3-course',
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0-pending, 1-confirmed, 2-completed, 3-cancelled',
  `remarks` TEXT,
  `rating` TINYINT COMMENT '1-5',
  `review` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  INDEX idx_user_id (`user_id`),
  INDEX idx_type (`type`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Course table
DROP TABLE IF EXISTS `b_course`;
CREATE TABLE `b_course` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `coach_id` INT NOT NULL,
  `court_id` INT,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `max_count` INT DEFAULT 10,
  `current_count` INT DEFAULT 0,
  `price` DECIMAL(8,2) DEFAULT 0.00,
  `description` TEXT,
  `status` TINYINT DEFAULT 1 COMMENT '0-offline, 1-open, 2-full, 3-ended',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`court_id`) REFERENCES `b_court`(`id`) ON DELETE SET NULL,
  INDEX idx_coach_id (`coach_id`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Product table
DROP TABLE IF EXISTS `b_product`;
CREATE TABLE `b_product` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `type` TINYINT DEFAULT 1 COMMENT '1-equipment, 2-clothing, 3-accessories',
  `price` DECIMAL(8,2) NOT NULL,
  `stock` INT DEFAULT 0,
  `image` VARCHAR(255),
  `description` TEXT,
  `status` TINYINT DEFAULT 1 COMMENT '0-offline, 1-online',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Activity table
DROP TABLE IF EXISTS `b_activity`;
CREATE TABLE `b_activity` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT,
  `image` VARCHAR(255),
  `start_time` DATETIME,
  `end_time` DATETIME,
  `location` VARCHAR(200),
  `max_participants` INT DEFAULT 100,
  `current_participants` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1 COMMENT '0-cancelled, 1-signup, 2-ongoing, 3-ended',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Coach schedule table
DROP TABLE IF EXISTS `b_coach_schedule`;
CREATE TABLE `b_coach_schedule` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `coach_id` INT NOT NULL,
  `day_of_week` TINYINT NOT NULL COMMENT '1-Mon, 7-Sun',
  `start_time` TIME NOT NULL,
  `end_time` TIME NOT NULL,
  `status` TINYINT DEFAULT 1 COMMENT '0-unavailable, 1-available',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`coach_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  INDEX idx_coach_id (`coach_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Court maintenance table
DROP TABLE IF EXISTS `b_court_maintenance`;
CREATE TABLE `b_court_maintenance` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `court_id` INT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `reason` VARCHAR(200),
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`court_id`) REFERENCES `b_court`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Notification table
DROP TABLE IF EXISTS `b_notification`;
CREATE TABLE `b_notification` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT,
  `type` TINYINT DEFAULT 1,
  `is_read` TINYINT DEFAULT 0 COMMENT '0-unread, 1-read',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  INDEX idx_user_id (`user_id`),
  INDEX idx_is_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Operation log table
DROP TABLE IF EXISTS `b_operation_log`;
CREATE TABLE `b_operation_log` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `admin_id` INT NOT NULL,
  `action` VARCHAR(100) NOT NULL,
  `target` VARCHAR(200),
  `ip` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_admin_id (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Check-in table
DROP TABLE IF EXISTS `b_checkin`;
CREATE TABLE `b_checkin` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `checkin_date` DATE NOT NULL,
  `points_earned` INT DEFAULT 1,
  `continuous_days` INT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `b_user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY uk_user_date (`user_id`, `checkin_date`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin account (admin/123456, password is BCrypt hashed)
INSERT INTO `b_user` (`username`, `password`, `role`, `status`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 0, 1);

-- 13. Coach table (独立教练信息表)
DROP TABLE IF EXISTS `b_coach`;
CREATE TABLE `b_coach` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `real_name` VARCHAR(100) NOT NULL,
  `gender` INT DEFAULT 0 COMMENT '0-未知 1-男 2-女',
  `age` INT,
  `phone` VARCHAR(20),
  `avatar` VARCHAR(500),
  `introduction` TEXT,
  `skills` TEXT,
  `hourly_rate` DECIMAL(10, 2) DEFAULT 0,
  `status` INT DEFAULT 1 COMMENT '0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';
