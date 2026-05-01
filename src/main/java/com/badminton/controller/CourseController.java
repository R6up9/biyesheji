package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Course;
import com.badminton.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/course")
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/list")
    public Result<Page<Course>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            wrapper.like(Course::getName, name);
        }
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }

        wrapper.orderByDesc(Course::getCreateTime);
        return Result.success(courseMapper.selectPage(page, wrapper));
    }

    @GetMapping("/all")
    public Result<java.util.List<Course>> all() {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 1);
        wrapper.orderByDesc(Course::getCreateTime);
        return Result.success(courseMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Integer id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        return Result.success(course);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Course course) {
        if (course.getName() == null || course.getName().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        if (course.getCoachId() == null) {
            return Result.error("请选择教练");
        }
        if (course.getStartTime() == null || course.getEndTime() == null) {
            return Result.error("请选择上课时间");
        }
        if (course.getMaxCount() == null || course.getMaxCount() <= 0) {
            return Result.error("请设置最大人数");
        }
        if (course.getPrice() == null || course.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            return Result.error("请设置课程价格");
        }

        course.setCurrentCount(0);
        if (course.getStatus() == null) {
            course.setStatus(1);
        }

        courseMapper.insert(course);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Course course) {
        if (course.getId() == null) {
            return Result.error("课程ID不能为空");
        }
        courseMapper.updateById(course);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        courseMapper.deleteById(id);
        return Result.success();
    }
}
