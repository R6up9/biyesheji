package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.User;
import com.badminton.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/account")
public class AccountController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    public Result<Page<User>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String username) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 0); // 只查管理员
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(User::getUsername, username.trim());
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id) {
        return Result.success(userMapper.selectById(id));
    }

    @PostMapping
    @Transactional
    public Result<Void> add(@RequestBody User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("账号已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setRole(0); // 固定为管理员
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping
    @Transactional
    public Result<Void> update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("ID不能为空");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        wrapper.ne(User::getId, user.getId());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("账号已存在");
        }
        // 密码不为空才更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        // 不能删除自己
        User currentUser = userMapper.selectById(id);
        if (currentUser != null && "admin".equals(currentUser.getUsername())) {
            return Result.error("不能删除admin账号");
        }
        userMapper.deleteById(id);
        return Result.success();
    }
}
