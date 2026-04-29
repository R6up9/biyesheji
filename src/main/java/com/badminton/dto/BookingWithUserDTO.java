package com.badminton.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingWithUserDTO {
    private Integer id;
    private Integer userId;
    private Integer targetId;
    private Integer type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private BigDecimal amount;
    private String remarks;
    private Integer rating;
    private String review;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 用户信息
    private String userName;
    private String avatar;
}
