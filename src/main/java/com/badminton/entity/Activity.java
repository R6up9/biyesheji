package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String content;

    private String image;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    private String location;

    @TableField("max_participants")
    private Integer maxParticipants;

    @TableField("current_participants")
    private Integer currentParticipants;

    private BigDecimal price;

    /** 0-已取消, 1-报名中, 2-进行中, 3-已结束 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
