package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_booking")
public class Booking {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("target_id")
    private Integer targetId;

    /** 1-场地, 2-教练, 3-课程 */
    private Integer type;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    /** 0-待确认, 1-已确认, 2-已完成, 3-已取消 */
    private Integer status;

    private BigDecimal amount;

    private String remarks;

    private Integer rating;

    private String review;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
