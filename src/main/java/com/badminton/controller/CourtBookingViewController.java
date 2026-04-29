package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.entity.User;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.CourtMapper;
import com.badminton.mapper.UserMapper;
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
@RequestMapping("/api/admin/court-visualization")
public class CourtBookingViewController {

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private UserMapper userMapper;

    // 获取场地预约可视化数据
    @GetMapping("/data")
    public Result<Map<String, Object>> getBookingVisualization(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);

        // 获取所有可用场地
        LambdaQueryWrapper<Court> courtWrapper = new LambdaQueryWrapper<>();
        courtWrapper.orderByAsc(Court::getId);
        List<Court> courts = courtMapper.selectList(courtWrapper);

        List<Map<String, Object>> courtDataList = new ArrayList<>();

        for (Court court : courts) {
            Map<String, Object> courtData = new HashMap<>();
            courtData.put("id", court.getId());
            courtData.put("name", court.getName());
            courtData.put("location", court.getLocation());
            courtData.put("capacity", court.getCapacity());

            // 时间段：11:00-16:00
            int startHour = 11;
            int endHour = 16;
            LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.of(startHour, 0));
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.of(endHour, 0));

            // 查询该时段所有预约
            LambdaQueryWrapper<Booking> bookingWrapper = new LambdaQueryWrapper<>();
            bookingWrapper.eq(Booking::getTargetId, court.getId());
            bookingWrapper.eq(Booking::getType, 1);
            bookingWrapper.eq(Booking::getStartTime, startTime);
            bookingWrapper.in(Booking::getStatus, 0, 1, 2);
            bookingWrapper.orderByAsc(Booking::getCreateTime);

            List<Booking> bookings = bookingMapper.selectList(bookingWrapper);

            List<Map<String, Object>> bookingDetails = new ArrayList<>();
            for (Booking booking : bookings) {
                User user = userMapper.selectById(booking.getUserId());
                Map<String, Object> detail = new HashMap<>();
                detail.put("bookingId", booking.getId());
                detail.put("userId", booking.getUserId());
                detail.put("userName", user != null ? user.getUsername() : "未知用户");
                detail.put("status", booking.getStatus());
                detail.put("statusLabel", getStatusLabel(booking.getStatus()));
                detail.put("createTime", booking.getCreateTime().toString());
                bookingDetails.add(detail);
            }

            int bookedCount = bookings.size();
            int remainingCount = court.getCapacity() - bookedCount;

            Map<String, Object> timeSlot = new HashMap<>();
            timeSlot.put("timeRange", "11:00-16:00");
            timeSlot.put("startTime", startTime.toString());
            timeSlot.put("endTime", endTime.toString());
            timeSlot.put("bookedCount", bookedCount);
            timeSlot.put("remainingCount", remainingCount);
            timeSlot.put("capacity", court.getCapacity());
            timeSlot.put("isFull", remainingCount <= 0);
            timeSlot.put("bookings", bookingDetails);

            courtData.put("timeSlot", timeSlot);
            courtDataList.add(courtData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("courts", courtDataList);

        return Result.success(result);
    }

    private String getStatusLabel(int status) {
        switch (status) {
            case 0: return "待确认";
            case 1: return "已确认";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}
