package com.badminton.controller;

import com.badminton.common.Result;
import com.badminton.dto.LoginDTO;
import com.badminton.dto.RegisterDTO;
import com.badminton.entity.Member;
import com.badminton.entity.User;
import com.badminton.service.AuthService;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO dto) {
        return authService.register(dto);
    }

    @GetMapping("/currentUser")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getHeader("x-access-token");
        }
        if (token == null) return Result.error("未登录");
        Integer userId = jwtUtils.getUserIdFromToken(token);
        return authService.getCurrentUser(userId);
    }

    @GetMapping("/memberInfo")
    public Result<Member> getMemberInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getHeader("x-access-token");
        }
        if (token == null) return Result.error("未登录");
        Integer userId = jwtUtils.getUserIdFromToken(token);
        return authService.getMemberInfo(userId);
    }

    @GetMapping("/initAdmin")
    public Result<String> initAdmin() {
        String hash = passwordEncoder.encode("123456");
        return Result.success(hash);
    }
    
    @GetMapping("/fixAdminPassword")
    public Result<String> fixAdminPassword() {
        String hash = passwordEncoder.encode("123456");
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        User admin = authService.getUserMapper().selectOne(wrapper);
        if (admin != null) {
            admin.setPassword(hash);
            authService.getUserMapper().updateById(admin);
            return Result.success("管理员密码已修复，请使用 admin/123456 登录");
        }
        return Result.error("admin 账号不存在");
    }

    @PutMapping("/updateAvatar")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> data, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getHeader("x-access-token");
        }
        if (token == null) return Result.error("未登录");
        Integer userId = jwtUtils.getUserIdFromToken(token);
        String avatar = data.get("avatar");
        return authService.updateAvatar(userId, avatar);
    }
}
