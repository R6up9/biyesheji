package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.dto.BookingWithUserDTO;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.entity.Course;
import com.badminton.entity.Activity;
import com.badminton.entity.Coach;
import com.badminton.entity.User;
import com.badminton.entity.Member;
import com.badminton.mapper.BookingMapper;
import com.badminton.mapper.CourtMapper;
import com.badminton.mapper.CourseMapper;
import com.badminton.mapper.ActivityMapper;
import com.badminton.mapper.CoachMapper;
import com.badminton.mapper.UserMapper;
import com.badminton.mapper.MemberMapper;
import com.badminton.service.PaymentService;
import com.badminton.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/admin/booking/list")
    public Result<Page<BookingWithUserDTO>> adminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Booking> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();

            if (type != null) {
                wrapper.eq(Booking::getType, type);
            }
            if (status != null) {
                wrapper.eq(Booking::getStatus, status);
            }

            wrapper.orderByDesc(Booking::getCreateTime);
            Page<Booking> bookingPage = bookingMapper.selectPage(page, wrapper);

            // 转换为DTO并添加用户信息
            Page<BookingWithUserDTO> dtoPage = new Page<>(pageNum, pageSize, bookingPage.getTotal());
            List<BookingWithUserDTO> dtoList = new ArrayList<>();

            for (Booking booking : bookingPage.getRecords()) {
                BookingWithUserDTO dto = new BookingWithUserDTO();
                BeanUtils.copyProperties(booking, dto);

                // 添加用户信息
                if (booking.getUserId() != null) {
                    User user = userMapper.selectById(booking.getUserId());
                    if (user != null) {
                        dto.setUserName(user.getUsername());
                    }

                    // 添加会员头像
                    Member member = memberMapper.selectOne(new LambdaQueryWrapper<Member>().eq(Member::getUserId, booking.getUserId()));
                    if (member != null) {
                        dto.setAvatar(member.getAvatar());
                    }
                }

                dtoList.add(dto);
            }

            dtoPage.setRecords(dtoList);
            return Result.success(dtoPage);
        } catch (Exception e) {
            System.out.println("Error in BookingController.adminList: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取预约列表失败");
        }
    }

    @PutMapping("/admin/booking")
    public Result<String> update(@RequestBody Booking booking) {
        try {
            Booking existBooking = bookingMapper.selectById(booking.getId());
            if (existBooking == null) {
                return Result.error("预约不存在");
            }

            Integer oldStatus = existBooking.getStatus();
            Integer newStatus = booking.getStatus();
            booking.setUpdateTime(LocalDateTime.now());
            bookingMapper.updateById(booking);

            // 只在真正有状态变化时更新相关数量（并且避免重复计算）
            if (!oldStatus.equals(newStatus)) {
                if (existBooking.getType() == 2) { // 课程
                    Course course = courseMapper.selectById(existBooking.getTargetId());
                    if (course != null) {
                        // 只有状态从有效状态（0或1）变为取消时，减1（完成不减少人数）
                        if ((oldStatus == 0 || oldStatus == 1) && newStatus == 3) {
                            if (course.getCurrentCount() > 0) {
                                course.setCurrentCount(course.getCurrentCount() - 1);
                                courseMapper.updateById(course);
                            }
                        }
                        // 只有状态从0变为1时，才加1（避免客户端已经是直接创建状态为1的booking被重复加1）
                        else if (oldStatus == 0 && newStatus == 1) {
                            course.setCurrentCount(course.getCurrentCount() + 1);
                            courseMapper.updateById(course);
                        }
                    }
                } else if (existBooking.getType() == 3) { // 活动
                    Activity activity = activityMapper.selectById(existBooking.getTargetId());
                    if (activity != null) {
                        // 只有状态从有效状态（0或1）变为取消时，减1（完成不减少人数）
                        if ((oldStatus == 0 || oldStatus == 1) && newStatus == 3) {
                            if (activity.getCurrentParticipants() > 0) {
                                activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                                activityMapper.updateById(activity);
                            }
                        }
                        // 只有状态从0变为1时，才加1（避免客户端已经创建状态为1的booking被重复加1
                        else if (oldStatus == 0 && newStatus == 1) {
                            activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
                            activityMapper.updateById(activity);
                        }
                    }
                }
            }

            return Result.successMsg("更新成功");
        } catch (Exception e) {
            System.out.println("Error in BookingController.update: " + e.getMessage());
            e.printStackTrace();
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/admin/booking/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        try {
            bookingMapper.deleteById(id);
            return Result.successMsg("删除成功");
        } catch (Exception e) {
            System.out.println("Error in BookingController.delete: " + e.getMessage());
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    @PostMapping("/user/booking")
    public Result<String> createBooking(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("x-access-token");
            Integer userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error("请先登录");
            }

            Integer targetId = (Integer) request.get("targetId");
            Integer type = (Integer) request.get("type");
            String startTimeStr = (String) request.get("startTime");
            String endTimeStr = (String) request.get("endTime");

            BigDecimal amount = BigDecimal.ZERO;

            if (type == 1) {
                Court court = courtMapper.selectById(targetId);
                if (court == null || court.getStatus() != 1) {
                    return Result.error("场地不可用");
                }

                LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
                LocalDateTime startOfDay = startTime.toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = startTime.toLocalDate().atTime(23, 59, 59);

                LambdaQueryWrapper<Booking> checkUserWrapper = new LambdaQueryWrapper<>();
                checkUserWrapper.eq(Booking::getUserId, userId);
                checkUserWrapper.eq(Booking::getTargetId, targetId);
                checkUserWrapper.eq(Booking::getType, 1);
                checkUserWrapper.ge(Booking::getStartTime, startOfDay);
                checkUserWrapper.le(Booking::getStartTime, endOfDay);
                checkUserWrapper.in(Booking::getStatus, 0, 1, 2);

                if (bookingMapper.selectOne(checkUserWrapper) != null) {
                    return Result.error("您今天已经预约过该场地了，每人每天每场只能预约一次");
                }

                LambdaQueryWrapper<Booking> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(Booking::getTargetId, targetId);
                countWrapper.eq(Booking::getType, 1);
                countWrapper.eq(Booking::getStartTime, startTime);
                countWrapper.in(Booking::getStatus, 0, 1);

                Long currentCount = bookingMapper.selectCount(countWrapper);
                if (currentCount >= court.getCapacity()) {
                    return Result.error("该时段预约人数已满");
                }

                // 计算场地价格
                if (court.getPricePerHour() != null) {
                    LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
                    long hours = java.time.Duration.between(startTime, endTime).toHours();
                    amount = court.getPricePerHour().multiply(new BigDecimal(Math.max(hours, 1)));
                }
            } else if (type == 2) {
                Course course = courseMapper.selectById(targetId);
                if (course == null || course.getStatus() != 1) {
                    return Result.error("课程不可用");
                }
                if (course.getPrice() != null) {
                    amount = course.getPrice();
                }
            } else if (type == 3) {
                Activity activity = activityMapper.selectById(targetId);
                if (activity == null || activity.getStatus() != 1) {
                    return Result.error("活动不可用");
                }
                if (activity.getPrice() != null) {
                    amount = activity.getPrice();
                }
            } else if (type == 4) {
                Coach coach = coachMapper.selectById(targetId);
                if (coach == null || coach.getStatus() != 1) {
                    return Result.error("教练不可用");
                }
                if (coach.getHourlyRate() != null) {
                    amount = coach.getHourlyRate();
                }
            }

            // 检查余额并扣款
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

            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setTargetId(targetId);
            booking.setType(type);
            booking.setStartTime(LocalDateTime.parse(startTimeStr));
            booking.setEndTime(LocalDateTime.parse(endTimeStr));
            booking.setStatus(1); // 直接已确认（已付款）
            booking.setAmount(amount);
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());

            bookingMapper.insert(booking);

            // 更新课程/活动人数
            if (type == 2) {
                Course course = courseMapper.selectById(targetId);
                if (course != null) {
                    course.setCurrentCount(course.getCurrentCount() + 1);
                    courseMapper.updateById(course);
                }
            } else if (type == 3) {
                Activity activity = activityMapper.selectById(targetId);
                if (activity != null) {
                    activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
                    activityMapper.updateById(activity);
                }
            }

            return Result.successMsg("预约成功！" + (amount.compareTo(BigDecimal.ZERO) > 0 ? "已扣款：" + amount : ""));
        } catch (Exception e) {
            System.out.println("Error in BookingController.createBooking: " + e.getMessage());
            e.printStackTrace();
            return Result.error("预约失败，请稍后重试");
        }
    }

    @GetMapping("/user/booking/my")
  public Result<Page<Booking>> myBookings(
          @RequestParam(defaultValue = "1") Integer pageNum,
          @RequestParam(defaultValue = "8") Integer pageSize,
          @RequestParam(required = false) Integer status,
          HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("x-access-token");
            Integer userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error("请先登录");
            }

            Page<Booking> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Booking> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Booking::getUserId, userId);

            if (status != null) {
                wrapper.eq(Booking::getStatus, status);
            }

            wrapper.orderByDesc(Booking::getCreateTime);
            return Result.success(bookingMapper.selectPage(page, wrapper));
        } catch (Exception e) {
            System.out.println("Error in BookingController.myBookings: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取预约列表失败");
        }
    }

    @PutMapping("/user/booking/{id}/cancel")
    public Result<String> cancelBooking(
            @PathVariable Integer id,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("x-access-token");
            Integer userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error("请先登录");
            }

            Booking booking = bookingMapper.selectById(id);
            if (booking == null) {
                return Result.error("预约不存在");
            }

            if (!booking.getUserId().equals(userId)) {
                return Result.error("只能取消自己的预约");
            }

            if (booking.getStatus() == 2 || booking.getStatus() == 3) {
                return Result.error("该预约已完成或已取消");
            }

            Integer oldStatus = booking.getStatus();
            booking.setStatus(3);
            booking.setUpdateTime(LocalDateTime.now());
            bookingMapper.updateById(booking);

            // 如果预约有金额，需要退款
            if (booking.getAmount() != null && booking.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                boolean refundSuccess = paymentService.refundBalance(userId, booking.getAmount());
                if (!refundSuccess) {
                    System.out.println("退款失败，userId: " + userId + ", amount: " + booking.getAmount());
                }
            }

            if (booking.getType() == 2 && oldStatus != 0) {
                Course course = courseMapper.selectById(booking.getTargetId());
                if (course != null && course.getCurrentCount() > 0) {
                    course.setCurrentCount(course.getCurrentCount() - 1);
                    courseMapper.updateById(course);
                }
            } else if (booking.getType() == 3 && oldStatus != 0) {
                Activity activity = activityMapper.selectById(booking.getTargetId());
                if (activity != null && activity.getCurrentParticipants() > 0) {
                    activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
                    activityMapper.updateById(activity);
                }
            }

            return Result.successMsg("预约已取消" + (booking.getAmount() != null && booking.getAmount().compareTo(BigDecimal.ZERO) > 0 ? "，已退款：" + booking.getAmount() : ""));
        } catch (Exception e) {
            System.out.println("Error in BookingController.cancelBooking: " + e.getMessage());
            e.printStackTrace();
            return Result.error("取消失败，请稍后重试");
        }
    }

    @GetMapping("/admin/booking/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            LambdaQueryWrapper<Booking> totalWrapper = new LambdaQueryWrapper<>();
            stats.put("total", bookingMapper.selectCount(totalWrapper));

            LambdaQueryWrapper<Booking> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(Booking::getStatus, 0);
            stats.put("pending", bookingMapper.selectCount(pendingWrapper));

            LambdaQueryWrapper<Booking> confirmedWrapper = new LambdaQueryWrapper<>();
            confirmedWrapper.eq(Booking::getStatus, 1);
            stats.put("confirmed", bookingMapper.selectCount(confirmedWrapper));

            LambdaQueryWrapper<Booking> completedWrapper = new LambdaQueryWrapper<>();
            completedWrapper.eq(Booking::getStatus, 2);
            stats.put("completed", bookingMapper.selectCount(completedWrapper));

            Map<String, Long> typeStats = new HashMap<>();
            for (int type = 1; type <= 3; type++) {
                LambdaQueryWrapper<Booking> typeWrapper = new LambdaQueryWrapper<>();
                typeWrapper.eq(Booking::getType, type);
                typeStats.put("type" + type, bookingMapper.selectCount(typeWrapper));
            }
            stats.put("typeStats", typeStats);

            return Result.success(stats);
        } catch (Exception e) {
            System.out.println("Error in BookingController.getStats: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取统计数据失败");
        }
    }
}
