package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    @TableField("coach_id")
    private Integer coachId;

    @TableField("court_id")
    private Integer courtId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("max_count")
    private Integer maxCount;

    @TableField("current_count")
    private Integer currentCount;

    private BigDecimal price;

    private String description;

    /** 0-下架, 1-开放报名, 2-已满, 3-已结束 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
