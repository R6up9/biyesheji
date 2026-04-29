package com.badminton.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.badminton.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
