package com.badminton.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_coach")
public class Coach {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 真实姓名 */
    private String realName;

    /** 性别 0-未知 1-男 2-女 */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 联系电话 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 简介 */
    private String introduction;

    /** 特长/资质 */
    private String skills;

    /** 课时费 */
    private BigDecimal hourlyRate;

    /** 状态 0-禁用 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
