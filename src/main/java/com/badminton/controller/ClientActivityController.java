package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Activity;
import com.badminton.entity.Booking;
import com.badminton.mapper.ActivityMapper;
import com.badminton.mapper.BookingMapper;
import com.badminton.service.PaymentService;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ClientActivityController {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Result<List<Activity>> getAll() {
        try {
            LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Activity::getStatus, 1); // 只显示开放报名的
            wrapper.orderByDesc(Activity::getCreateTime);
            return Result.success(activityMapper.selectList(wrapper));
        } catch (Exception e) {
            System.out.println("Error in ClientActivityController.getAll: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取活动列表失败");
        }
    }

    @GetMapping("/{id}")
    public Result<Activity> getById(@PathVariable Integer id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            return Result.success(activity);
        } catch (Exception e) {
            System.out.println("Error in ClientActivityController.getById: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取活动详情失败");
        }
    }

    @PostMapping("/{id}/enroll")
    public Result<String> enroll(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("x-access-token");
            Integer userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error("请先登录");
            }

            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            if (activity.getStatus() != 1) {
                return Result.error("该活动暂不开放报名");
            }
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                return Result.error("该活动报名人数已满");
            }

            // 检查是否已经报名过
            LambdaQueryWrapper<Booking> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Booking::getUserId, userId);
            checkWrapper.eq(Booking::getTargetId, id);
            checkWrapper.eq(Booking::getType, 3); // 3=活动
            checkWrapper.in(Booking::getStatus, 0, 1, 2); // 待确认/已确认/已完成
            if (bookingMapper.selectOne(checkWrapper) != null) {
                return Result.error("您已经报名过该活动");
            }

            BigDecimal amount = activity.getPrice() != null ? activity.getPrice() : BigDecimal.ZERO;

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
            booking.setType(3);
            booking.setStartTime(activity.getStartTime());
            booking.setEndTime(activity.getEndTime());
            booking.setStatus(1); // 直接已确认
            booking.setAmount(amount);
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());

            bookingMapper.insert(booking);

            // 更新活动当前人数
            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
            activityMapper.updateById(activity);

            return Result.successMsg("报名成功！" + (amount.compareTo(BigDecimal.ZERO) > 0 ? "已扣款：" + amount : ""));
        } catch (Exception e) {
            System.out.println("Error in ClientActivityController.enroll: " + e.getMessage());
            e.printStackTrace();
            return Result.error("报名失败，请稍后重试");
        }
    }
}
