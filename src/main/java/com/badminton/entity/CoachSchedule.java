package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("b_coach_schedule")
public class CoachSchedule {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer coachId;

    /** 1-周一, 7-周日 */
    private Integer dayOfWeek;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0-不可用, 1-可用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
