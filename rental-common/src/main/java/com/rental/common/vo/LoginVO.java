package com.rental.common.vo;

import lombok.Data;

/**
 * 登录成功响应VO
 */
@Data
public class LoginVO {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserVO userInfo;
    
    /**
     * Token过期时间（时间戳）
     */
    private Long expireTime;
}