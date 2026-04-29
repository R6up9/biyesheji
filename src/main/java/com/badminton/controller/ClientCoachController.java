package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Booking;
import com.badminton.entity.Coach;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.CoachMapper;
import com.badminton.service.PaymentService;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/coaches")
public class ClientCoachController {

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Result<List<Coach>> getAll() {
        LambdaQueryWrapper<Coach> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coach::getStatus, 1); // 只显示在职教练
        wrapper.orderByAsc(Coach::getId);
        return Result.success(coachMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Coach> getById(@PathVariable Integer id) {
        Coach coach = coachMapper.selectById(id);
        if (coach == null) {
            return Result.error("教练不存在");
        }
        return Result.success(coach);
    }

    @PostMapping("/{id}/book")
    public Result<String> book(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("x-access-token");
            Integer userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error("请先登录");
            }

            Coach coach = coachMapper.selectById(id);
            if (coach == null) {
                return Result.error("教练不存在");
            }
            if (coach.getStatus() != 1) {
                return Result.error("该教练暂不可预约");
            }

            // 检查是否已经预约过该教练（待确认/已确认状态）
            LambdaQueryWrapper<Booking> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Booking::getUserId, userId);
            checkWrapper.eq(Booking::getTargetId, id);
            checkWrapper.eq(Booking::getType, 4); // 4=教练预约
            checkWrapper.in(Booking::getStatus, 0, 1); // 待确认/已确认
            if (bookingMapper.selectOne(checkWrapper) != null) {
                return Result.error("您已经预约过该教练，请联系管理员确认预约时间");
            }

            BigDecimal amount = coach.getHourlyRate() != null ? coach.getHourlyRate() : BigDecimal.ZERO;

            // 如果有价格，检查余额并扣款
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentBalance = paymentService.getBalance(userId);
                if (currentBalance.compareTo(amount) < 0) {
                    return Result.error("余额不足！当前余额：" + currentBalance + "，请联系管理员充值");
                }
                boolean deductSuccess = paymentService.deductBalance(userId, amount);
                if (!deductSuccess) {
                    return Result.error("扣款失败，请联系管理员");
                }
            }

            // 创建预约记录
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setTargetId(id);
            booking.setType(4); // 教练预约
            // 设置预约时间（当前时间起1小时）
            LocalDateTime now = LocalDateTime.now();
            booking.setStartTime(now);
            booking.setEndTime(now.plusHours(1));
            booking.setStatus(0); // 待确认
            booking.setAmount(amount);
            booking.setCreateTime(now);
            booking.setUpdateTime(now);

            bookingMapper.insert(booking);

            return Result.successMsg("预约成功！请等待管理员确认预约时间。" + (amount.compareTo(BigDecimal.ZERO) > 0 ? "已扣款：" + amount : ""));
        } catch (Exception e) {
            System.out.println("Error in ClientCoachController.book: " + e.getMessage());
            e.printStackTrace();
            return Result.error("预约失败，请稍后重试");
        }
    }
}
