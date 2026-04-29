package com.badminton.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.dto.LoginDTO;
import com.badminton.dto.RegisterDTO;
import com.badminton.entity.Member;
import com.badminton.entity.User;
import com.badminton.mapper.MemberMapper;
import com.badminton.mapper.UserMapper;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public Result<Map<String, Object>> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            System.out.println("DEBUG: User not found: " + dto.getUsername());
            return Result.error("用户名或密码错误");
        }
        System.out.println("DEBUG: Found user: " + user.getUsername() + ", role=" + user.getRole() + ", status=" + user.getStatus());
        System.out.println("DEBUG: Password in DB: " + user.getPassword());
        System.out.println("DEBUG: Password input: " + dto.getPassword());
        
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        System.out.println("DEBUG: Password matches: " + matches);
        if (!matches) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    public Result<Void> register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(1);
        user.setStatus(1);
        userMapper.insert(user);

        Member member = new Member();
        member.setUserId(user.getId());
        member.setRealName(dto.getRealName());
        member.setGender(dto.getGender() != null ? dto.getGender() : 0);
        member.setAge(dto.getAge());
        member.setPhone(dto.getPhone());
        member.setLevel(1);
        member.setPoints(0);
        memberMapper.insert(member);

        return Result.successMsg("注册成功");
    }

    public Result<User> getCurrentUser(Integer userId) {
        System.out.println("DEBUG: getCurrentUser called with userId=" + userId);
        User user = userMapper.selectById(userId);
        System.out.println("DEBUG: User found: " + user);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<Member> getMemberInfo(Integer userId) {
        System.out.println("DEBUG: getMemberInfo called with userId=" + userId);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(wrapper);
        System.out.println("DEBUG: Member found: " + member);
        return Result.success(member);
    }

    public UserMapper getUserMapper() {
        return userMapper;
    }

    public Result<Void> updateAvatar(Integer userId, String avatar) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(wrapper);
        if (member == null) {
            return Result.error("会员信息不存在");
        }
        member.setAvatar(avatar);
        memberMapper.updateById(member);
        return Result.successMsg("头像更新成功");
    }
}
