package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Activity;
import com.badminton.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity")
public class ActivityController {

    @Autowired
    private ActivityMapper activityMapper;

    @GetMapping("/list")
    public Result<Page<Activity>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Activity::getTitle, title.trim());
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }

        wrapper.orderByDesc(Activity::getCreateTime);
        return Result.success(activityMapper.selectPage(page, wrapper));
    }

    @GetMapping("/all")
    public Result<java.util.List<Activity>> all() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, 1);
        wrapper.orderByDesc(Activity::getCreateTime);
        return Result.success(activityMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Integer id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Activity activity) {
        if (activity.getTitle() == null || activity.getTitle().trim().isEmpty()) {
            return Result.error("活动标题不能为空");
        }
        if (activity.getStartTime() == null || activity.getEndTime() == null) {
            return Result.error("请选择活动时间");
        }
        if (activity.getMaxParticipants() == null || activity.getMaxParticipants() <= 0) {
            return Result.error("请设置最大参与人数");
        }

        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        if (activity.getCurrentParticipants() == null) {
            activity.setCurrentParticipants(0);
        }

        activityMapper.insert(activity);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Activity activity) {
        if (activity.getId() == null) {
            return Result.error("活动ID不能为空");
        }
        activityMapper.updateById(activity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        activityMapper.deleteById(id);
        return Result.success();
    }
}
