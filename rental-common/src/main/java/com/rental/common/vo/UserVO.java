package com.rental.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 用户类型：1-房东 2-租客
     */
    private Integer userType;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：0-禁用 1-正常
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}