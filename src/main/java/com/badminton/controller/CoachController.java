package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Coach;
import com.badminton.mapper.CoachMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coach")
public class CoachController {

    @Autowired
    private CoachMapper coachMapper;

    @GetMapping("/page")
    public Result<Page<Coach>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status
    ) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        if (realName != null && !realName.trim().isEmpty()) {
            wrapper.like(Coach::getRealName, realName.trim());
        }
        if (status != null) {
            wrapper.eq(Coach::getStatus, status);
        }
        wrapper.orderByDesc(Coach::getCreateTime);
        return Result.success(coachMapper.selectPage(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<Coach> getById(@PathVariable Integer id) {
        return Result.success(coachMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Coach coach) {
        if (coach.getStatus() == null) {
            coach.setStatus(1);
        }
        coachMapper.insert(coach);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Coach coach) {
        if (coach.getId() == null) {
            return Result.error("ID不能为空");
        }
        coachMapper.updateById(coach);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        coachMapper.deleteById(id);
        return Result.success();
    }
}
