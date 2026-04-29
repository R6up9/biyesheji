package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String realName;

    /** 0-未知, 1-男, 2-女 */
    private Integer gender;

    private Integer age;

    private String phone;

    private String avatar;

    private String introduction;

    private Integer level;

    private Integer points;

    private BigDecimal balance;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
