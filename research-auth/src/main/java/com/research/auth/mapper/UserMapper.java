package com.research.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.research.auth.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名查询用户
     */
    User selectUserByUsername(String username);
}