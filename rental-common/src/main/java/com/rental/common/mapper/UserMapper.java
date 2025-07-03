package com.rental.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User findByPhone(String phone);
    
    /**
     * 更新最后登录时间
     * @param userId 用户ID
     * @return 更新行数
     */
    @Update("UPDATE users SET last_login_at = NOW() WHERE id = #{userId}")
    int updateLastLoginTime(Long userId);
}