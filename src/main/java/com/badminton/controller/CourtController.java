package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.CourtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/court")
public class CourtController {

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private BookingMapper bookingMapper;

    // 分页查询场地列表
    @GetMapping("/list")
    public Result<Page<Court>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        System.out.println("CourtController.list 被调用: pageNum=" + pageNum + ", pageSize=" + pageSize + ", name=" + name + ", status=" + status);
        
        Page<Court> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Court> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(Court::getName, name);
        }
        if (status != null) {
            wrapper.eq(Court::getStatus, status);
        }
        
        wrapper.orderByDesc(Court::getCreateTime);
        Page<Court> resultPage = courtMapper.selectPage(page, wrapper);
        
        System.out.println("查询结果: total=" + resultPage.getTotal() + ", records size=" + resultPage.getRecords().size());
        for (Court court : resultPage.getRecords()) {
            System.out.println("Court: id=" + court.getId() + ", name=" + court.getName());
        }
        
        return Result.success(resultPage);
    }

    // 获取所有可用场地
    @GetMapping("/all")
    public Result<List<Court>> all() {
        LambdaQueryWrapper<Court> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Court::getStatus, 1);
        return Result.success(courtMapper.selectList(wrapper));
    }

    // 获取单个场地详情
    @GetMapping("/{id}")
    public Result<Court> detail(@PathVariable Integer id) {
        Court court = courtMapper.selectById(id);
        if (court == null) {
            return Result.error("场地不存在");
        }
        return Result.success(court);
    }

    // 新增场地
    @PostMapping
    public Result<String> add(@RequestBody Court court) {
        court.setCreateTime(LocalDateTime.now());
        court.setUpdateTime(LocalDateTime.now());
        courtMapper.insert(court);
        return Result.successMsg("添加成功");
    }

    // 更新场地
    @PutMapping
    public Result<String> update(@RequestBody Court court) {
        court.setUpdateTime(LocalDateTime.now());
        courtMapper.updateById(court);
        return Result.successMsg("更新成功");
    }

    // 删除场地
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        courtMapper.deleteById(id);
        return Result.successMsg("删除成功");
    }

    // 获取场地的可预约时段
    @GetMapping("/{id}/time-slots")
    public Result<List<Map<String, Object>>> getTimeSlots(
            @PathVariable Integer id,
            @RequestParam String date) {
        
        LocalDate localDate = LocalDate.parse(date);
        List<Map<String, Object>> timeSlots = new ArrayList<>();
        
        // 每天8:00-22:00，每小时一个时段
        for (int hour = 8; hour < 22; hour++) {
            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.of(hour, 0));
            LocalDateTime endTime = startTime.plusHours(1);
            
            // 查询这个时段是否已被预约
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Booking::getTargetId, id);
            wrapper.eq(Booking::getType, 1); // 1=场地
            wrapper.eq(Booking::getStartTime, startTime);
            wrapper.in(Booking::getStatus, 0, 1); // 0=待确认, 1=已确认
            
            Booking existingBooking = bookingMapper.selectOne(wrapper);
            
            Map<String, Object> slot = new HashMap<>();
            slot.put("startTime", startTime.toString());
            slot.put("endTime", endTime.toString());
            slot.put("available", existingBooking == null);
            slot.put("booking", existingBooking);
            timeSlots.add(slot);
        }
        
        return Result.success(timeSlots);
    }
}
