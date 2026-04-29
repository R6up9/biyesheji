package com.badminton.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberWithUserDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private String password;
    private Integer role;
    private Integer status;
    private String realName;
    private Integer gender;
    private Integer age;
    private String phone;
    private String avatar;
    private String introduction;
    private Integer level;
    private Integer points;
    private BigDecimal balance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
