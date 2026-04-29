package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.CourtMapper;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courts")
public class ClientCourtController {

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private BookingMapper bookingMapper;
    
    @Autowired
    private JwtUtils jwtUtils;

    // 获取所有场地
    @GetMapping
    public Result<List<Court>> getAllCourts() {
        LambdaQueryWrapper<Court> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Court::getId);
        return Result.success(courtMapper.selectList(wrapper));
    }

    // 获取场地各时段预约情况
    @GetMapping("/{courtId}/bookings")
    public Result<Map<String, Object>> getCourtBookings(
            @PathVariable Integer courtId,
            @RequestParam String date,
            HttpServletRequest httpRequest) {
        LocalDate localDate = LocalDate.parse(date);
        
        // 获取场地信息
        Court court = courtMapper.selectById(courtId);
        if (court == null) {
            return Result.error("场地不存在");
        }
        
        // 生成一天的时间段：11:00-16:00
        List<Map<String, Object>> timeSlots = new ArrayList<>();
        
        // 时间段：11:00-16:00
        int startHour = 11;
        int endHour = 16;
        String timeRange = String.format("%02d:00-%02d:00", startHour, endHour);
        
        LocalDateTime startTime = LocalDateTime.of(localDate, LocalTime.of(startHour, 0));
        LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.of(endHour, 0));
        
        // 统计该时段预约人数（状态为待确认或已确认）
        LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Booking::getTargetId, courtId);
        wrapper.eq(Booking::getType, 1);
        wrapper.eq(Booking::getStartTime, startTime);
        wrapper.in(Booking::getStatus, 0, 1, 2);
        
        Long bookedCount = bookingMapper.selectCount(wrapper);
        int remainingCount = court.getCapacity() - bookedCount.intValue();
        
        // 检查当前用户今天是否已经预约过该场地
        boolean userHasBooked = false;
        String token = httpRequest.getHeader("x-access-token");
        Integer userId = jwtUtils.getUserIdFromToken(token);
        if (userId != null) {
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
            
            LambdaQueryWrapper<Booking> userBookingWrapper = new LambdaQueryWrapper<>();
            userBookingWrapper.eq(Booking::getUserId, userId);
            userBookingWrapper.eq(Booking::getTargetId, courtId);
            userBookingWrapper.eq(Booking::getType, 1);
            userBookingWrapper.ge(Booking::getStartTime, startOfDay);
            userBookingWrapper.le(Booking::getStartTime, endOfDay);
            userBookingWrapper.in(Booking::getStatus, 0, 1, 2);
            
            userHasBooked = bookingMapper.selectOne(userBookingWrapper) != null;
        }
        
        Map<String, Object> slot = new HashMap<>();
        slot.put("timeRange", timeRange);
        slot.put("startTime", startTime.toString());
        slot.put("endTime", endTime.toString());
        slot.put("bookedCount", bookedCount);
        slot.put("remainingCount", remainingCount);
        slot.put("capacity", court.getCapacity());
        slot.put("isFull", remainingCount <= 0);
        slot.put("userHasBooked", userHasBooked);
        
        timeSlots.add(slot);
        
        Map<String, Object> result = new HashMap<>();
        result.put("court", court);
        result.put("timeSlots", timeSlots);
        result.put("userHasBooked", userHasBooked);
        
        return Result.success(result);
    }
}