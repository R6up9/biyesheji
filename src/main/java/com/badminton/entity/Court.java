package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_court")
public class Court {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String location;

    private Integer capacity;

    /** 0-维护中, 1-可用 */
    private Integer status;

    @TableField("price_per_hour")
    private BigDecimal pricePerHour;

    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
