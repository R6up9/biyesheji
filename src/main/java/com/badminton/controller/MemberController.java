package com.badminton.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badminton.common.Result;
import com.badminton.dto.MemberWithUserDTO;
import com.badminton.entity.Member;
import com.badminton.entity.User;
import com.badminton.mapper.MemberMapper;
import com.badminton.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/member")
public class MemberController {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    public Result<Page<MemberWithUserDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) String realName,
                                                  @RequestParam(required = false) Integer gender,
                                                  @RequestParam(required = false) Integer level,
                                                  @RequestParam(required = false) String sortProp,
                                                  @RequestParam(required = false) String sortOrder) {
        System.out.println("查询参数: pageNum=" + pageNum + ", pageSize=" + pageSize + ", realName=" + realName + ", gender=" + gender + ", level=" + level + ", sortProp=" + sortProp + ", sortOrder=" + sortOrder);
        
        Page<Member> memberPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (realName != null && !realName.trim().isEmpty()) {
            wrapper.like(Member::getRealName, realName.trim());
        }
        if (gender != null) {
            wrapper.eq(Member::getGender, gender);
        }
        if (level != null) {
            wrapper.eq(Member::getLevel, level);
        }
        
        if (sortProp != null && !sortProp.trim().isEmpty()) {
            boolean isAsc = sortOrder != null && sortOrder.equals("ascending");
            switch (sortProp) {
                case "level":
                    if (isAsc) {
                        wrapper.orderByAsc(Member::getLevel);
                    } else {
                        wrapper.orderByDesc(Member::getLevel);
                    }
                    break;
                case "points":
                    if (isAsc) {
                        wrapper.orderByAsc(Member::getPoints);
                    } else {
                        wrapper.orderByDesc(Member::getPoints);
                    }
                    break;
                case "balance":
                    if (isAsc) {
                        wrapper.orderByAsc(Member::getBalance);
                    } else {
                        wrapper.orderByDesc(Member::getBalance);
                    }
                    break;
                default:
                    wrapper.orderByDesc(Member::getCreateTime);
            }
            if (isAsc) {
                wrapper.orderByAsc(Member::getBalance);
            } else {
                wrapper.orderByDesc(Member::getBalance);
            }
        } else {
            wrapper.orderByDesc(Member::getCreateTime);
        }
        
        memberMapper.selectPage(memberPage, wrapper);
        
        List<MemberWithUserDTO> list = new ArrayList<>();
        for (Member member : memberPage.getRecords()) {
            MemberWithUserDTO dto = convertToDTO(member);
            // 只显示普通会员（role=1），不显示管理员（role=0）
            if (dto.getRole() == null || dto.getRole() != 0) {
                list.add(dto);
            }
        }
        Page<MemberWithUserDTO> resultPage = new Page<>(pageNum, pageSize, list.size());
        resultPage.setRecords(list);
        
        System.out.println("查询结果总数: " + resultPage.getTotal());
        return Result.success(resultPage);
    }

    @GetMapping("/{id}")
    public Result<MemberWithUserDTO> getById(@PathVariable Integer id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        return Result.success(convertToDTO(member));
    }

    @PostMapping
    @Transactional
    public Result<Void> add(@RequestBody MemberWithUserDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(userWrapper) > 0) {
            return Result.error("账号已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRole(1);
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userMapper.insert(user);
        
        Member member = new Member();
        member.setUserId(user.getId());
        member.setRealName(dto.getRealName());
        member.setGender(dto.getGender());
        member.setAge(dto.getAge());
        member.setPhone(dto.getPhone());
        member.setAvatar(dto.getAvatar());
        member.setIntroduction(dto.getIntroduction());
        member.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        member.setPoints(dto.getPoints() != null ? dto.getPoints() : 0);
        member.setBalance(dto.getBalance() != null ? dto.getBalance() : java.math.BigDecimal.ZERO);
        memberMapper.insert(member);
        
        return Result.success();
    }

    @PutMapping
    @Transactional
    public Result<Void> update(@RequestBody MemberWithUserDTO dto) {
        if (dto.getId() == null) {
            return Result.error("会员ID不能为空");
        }
        
        Member member = memberMapper.selectById(dto.getId());
        if (member == null) {
            return Result.error("会员不存在");
        }
        
        if (member.getUserId() != null) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
                    LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.eq(User::getUsername, dto.getUsername());
                    userWrapper.ne(User::getId, user.getId());
                    if (userMapper.selectCount(userWrapper) > 0) {
                        return Result.error("账号已存在");
                    }
                    user.setUsername(dto.getUsername());
                }
                if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(dto.getPassword()));
                }
                if (dto.getStatus() != null) {
                    user.setStatus(dto.getStatus());
                }
                userMapper.updateById(user);
            }
        }
        
        member.setRealName(dto.getRealName());
        member.setGender(dto.getGender());
        member.setAge(dto.getAge());
        member.setPhone(dto.getPhone());
        // 不更新头像，保留原来的头像
        member.setIntroduction(dto.getIntroduction());
        member.setLevel(dto.getLevel());
        member.setPoints(dto.getPoints());
        member.setBalance(dto.getBalance());
        memberMapper.updateById(member);
        
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Integer id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }
        if (member.getUserId() != null) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null && "admin".equals(user.getUsername())) {
                return Result.error("不能删除 admin 管理员账号");
            }
            userMapper.deleteById(member.getUserId());
        }
        memberMapper.deleteById(id);
        return Result.success();
    }

    private MemberWithUserDTO convertToDTO(Member member) {
        MemberWithUserDTO dto = new MemberWithUserDTO();
        dto.setId(member.getId());
        dto.setUserId(member.getUserId());
        dto.setRealName(member.getRealName());
        dto.setGender(member.getGender());
        dto.setAge(member.getAge());
        dto.setPhone(member.getPhone());
        dto.setAvatar(member.getAvatar());
        dto.setIntroduction(member.getIntroduction());
        dto.setLevel(member.getLevel());
        dto.setPoints(member.getPoints());
        dto.setBalance(member.getBalance());
        dto.setCreateTime(member.getCreateTime());
        dto.setUpdateTime(member.getUpdateTime());
        
        if (member.getUserId() != null) {
            User user = userMapper.selectById(member.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setRole(user.getRole());
                dto.setStatus(user.getStatus());
            }
        }
        return dto;
    }

    @PutMapping("/recharge")
    @Transactional
    public Result<Map<String, Object>> recharge(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Object amountObj = params.get("amount");
        BigDecimal amount;

        if (id == null) {
            return Result.error("会员ID不能为空");
        }
        if (amountObj == null) {
            return Result.error("充值金额不能为空");
        }

        // 转换金额
        if (amountObj instanceof Integer) {
            amount = new BigDecimal((Integer) amountObj);
        } else if (amountObj instanceof Long) {
            amount = new BigDecimal((Long) amountObj);
        } else if (amountObj instanceof Double) {
            amount = BigDecimal.valueOf((Double) amountObj);
        } else if (amountObj instanceof String) {
            amount = new BigDecimal((String) amountObj);
        } else {
            amount = new BigDecimal(amountObj.toString());
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("充值金额必须大于0");
        }

        Member member = memberMapper.selectById(id);
        if (member == null) {
            return Result.error("会员不存在");
        }

        BigDecimal oldBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = oldBalance.add(amount);
        member.setBalance(newBalance);
        memberMapper.updateById(member);

        Map<String, Object> result = new HashMap<>();
        result.put("oldBalance", oldBalance);
        result.put("rechargeAmount", amount);
        result.put("newBalance", newBalance);

        System.out.println("充值成功，会员ID: " + id + "，充值金额: " + amount + "，新余额: " + newBalance);

        return Result.success(result);
    }
}
