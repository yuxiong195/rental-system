package com.rental.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {
    
    /**
     * 用户ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 手机号，唯一标识
     */
    private String phone;
    
    /**
     * 密码，房东使用，租客无需密码
     */
    private String password;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 用户类型：1-房东 2-租客
     */
    private Integer userType;
    
    /**
     * 微信openid，用于微信登录
     */
    private String wechatOpenid;
    
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
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}