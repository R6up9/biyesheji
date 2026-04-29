package com.badminton.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String realName;
    private Integer gender;
    private Integer age;
    private String phone;
}
