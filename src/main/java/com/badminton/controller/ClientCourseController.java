package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.common.Result;
import com.badminton.entity.Course;
import com.badminton.entity.Booking;
import com.badminton.mapper.CourseMapper;
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
@RequestMapping("/api/courses")
public class ClientCourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Result<List<Course>> getAll() {
        try {
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Course::getStatus, 1); // 只显示开放报名的
            wrapper.orderByDesc(Course::getCreateTime);
            return Result.success(courseMapper.selectList(wrapper));
        } catch (Exception e) {
            System.out.println("Error in ClientCourseController.getAll: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取课程列表失败");
        }
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Integer id) {
        try {
            Course course = courseMapper.selectById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }
            return Result.success(course);
        } catch (Exception e) {
            System.out.println("Error in ClientCourseController.getById: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取课程详情失败");
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

            Course course = courseMapper.selectById(id);
            if (course == null) {
                return Result.error("课程不存在");
            }
            if (course.getStatus() != 1) {
                return Result.error("该课程暂不开放报名");
            }
            if (course.getCurrentCount() >= course.getMaxCount()) {
                return Result.error("该课程报名人数已满");
            }

            // 检查是否已经报名过
            LambdaQueryWrapper<Booking> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(Booking::getUserId, userId);
            checkWrapper.eq(Booking::getTargetId, id);
            checkWrapper.eq(Booking::getType, 2); // 2=课程
            checkWrapper.in(Booking::getStatus, 0, 1, 2); // 待确认/已确认/已完成
            if (bookingMapper.selectOne(checkWrapper) != null) {
                return Result.error("您已经报名过该课程");
            }

            BigDecimal amount = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

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
            booking.setType(2);
            booking.setStartTime(course.getStartTime());
            booking.setEndTime(course.getEndTime());
            booking.setStatus(1); // 直接已确认
            booking.setAmount(amount);
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());

            bookingMapper.insert(booking);

            // 更新课程当前人数
            course.setCurrentCount(course.getCurrentCount() + 1);
            courseMapper.updateById(course);

            return Result.successMsg("报名成功！" + (amount.compareTo(BigDecimal.ZERO) > 0 ? "已扣款：" + amount : ""));
        } catch (Exception e) {
            System.out.println("Error in ClientCourseController.enroll: " + e.getMessage());
            e.printStackTrace();
            return Result.error("报名失败，请稍后重试");
        }
    }
}
