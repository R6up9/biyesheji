package com.badminton.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.badminton.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {
}
