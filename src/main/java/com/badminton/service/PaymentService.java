package com.badminton.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.badminton.entity.Member;
import com.badminton.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    private MemberMapper memberMapper;

    /**
     * 扣款
     *
     * @param userId 用户ID
     * @param amount 扣款金额
     * @return 是否扣款成功
     */
    public synchronized boolean deductBalance(Integer userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(wrapper);

        if (member == null) {
            return false;
        }

        if (member.getBalance() == null || member.getBalance().compareTo(amount) < 0) {
            return false; // 余额不足
        }

        member.setBalance(member.getBalance().subtract(amount));
        memberMapper.updateById(member);

        System.out.println("===== PaymentService.deductBalance =====");
        System.out.println("userId: " + userId + ", amount: " + amount);
        System.out.println("new balance: " + member.getBalance());

        return true;
    }

    /**
     * 退款
     *
     * @param userId 用户ID
     * @param amount 退款金额
     * @return 是否退款成功
     */
    public synchronized boolean refundBalance(Integer userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(wrapper);

        if (member == null) {
            return false;
        }

        if (member.getBalance() == null) {
            member.setBalance(BigDecimal.ZERO);
        }

        member.setBalance(member.getBalance().add(amount));
        memberMapper.updateById(member);

        System.out.println("===== PaymentService.refundBalance =====");
        System.out.println("userId: " + userId + ", amount: " + amount);
        System.out.println("new balance: " + member.getBalance());

        return true;
    }

    /**
     * 查询用户余额
     *
     * @param userId 用户ID
     * @return 余额
     */
    public BigDecimal getBalance(Integer userId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(wrapper);

        if (member == null) {
            return BigDecimal.ZERO;
        }

        return member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
    }
}
