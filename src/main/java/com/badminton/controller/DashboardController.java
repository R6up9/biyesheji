package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Booking;
import com.badminton.entity.Member;
import com.badminton.entity.ProductOrder;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.MemberMapper;
import com.badminton.mapper.ProductOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总预订数
        long totalBookings = bookingMapper.selectCount(null);
        stats.put("totalBookings", totalBookings);
        
        // 今日预订数
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        LambdaQueryWrapper<Booking> todayBookingWrapper = new LambdaQueryWrapper<>();
        todayBookingWrapper.ge(Booking::getCreateTime, startOfDay)
                          .le(Booking::getCreateTime, endOfDay);
        long todayBookings = bookingMapper.selectCount(todayBookingWrapper);
        stats.put("todayBookings", todayBookings);
        
        // 会员总数
        long totalMembers = memberMapper.selectCount(null);
        stats.put("totalMembers", totalMembers);
        
        // 订单总数
        long totalOrders = productOrderMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);
        
        // 订单总金额
        LambdaQueryWrapper<ProductOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(ProductOrder::getStatus, Arrays.asList(1, 2, 3)); // 已付款/已发货/已完成
        List<ProductOrder> paidOrders = productOrderMapper.selectList(orderWrapper);
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (ProductOrder order : paidOrders) {
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
        }
        stats.put("totalAmount", totalAmount);
        
        // 今日订单
        LambdaQueryWrapper<ProductOrder> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(ProductOrder::getCreateTime, startOfDay)
                        .le(ProductOrder::getCreateTime, endOfDay);
        long todayOrders = productOrderMapper.selectCount(todayOrderWrapper);
        stats.put("todayOrders", todayOrders);
        
        System.out.println("DEBUG: Dashboard stats: " + stats);
        return Result.success(stats);
    }

    @GetMapping("/booking/trend")
    public Result<List<Map<String, Object>>> getBookingTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Booking::getCreateTime, startOfDay)
                  .le(Booking::getCreateTime, endOfDay);
            
            long count = bookingMapper.selectCount(wrapper);
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("count", count);
            trend.add(item);
        }
        
        System.out.println("DEBUG: Booking trend: " + trend);
        return Result.success(trend);
    }

    @GetMapping("/booking/type")
    public Result<List<Map<String, Object>>> getBookingByType() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String[] types = {"场地预订", "课程报名", "活动报名"};
        for (int i = 1; i <= 3; i++) {
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Booking::getType, i);
            long count = bookingMapper.selectCount(wrapper);
            
            Map<String, Object> item = new HashMap<>();
            item.put("name", types[i - 1]);
            item.put("value", count);
            result.add(item);
        }
        
        System.out.println("DEBUG: Booking by type: " + result);
        return Result.success(result);
    }

    @GetMapping("/order/trend")
    public Result<List<Map<String, Object>>> getOrderTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            LambdaQueryWrapper<ProductOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(ProductOrder::getCreateTime, startOfDay)
                  .le(ProductOrder::getCreateTime, endOfDay);
            
            List<ProductOrder> orders = productOrderMapper.selectList(wrapper);
            java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
            for (ProductOrder order : orders) {
                if (order.getTotalAmount() != null) {
                    amount = amount.add(order.getTotalAmount());
                }
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("amount", amount);
            trend.add(item);
        }
        
        System.out.println("DEBUG: Order trend: " + trend);
        return Result.success(trend);
    }
}
