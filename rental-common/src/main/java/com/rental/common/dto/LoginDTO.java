package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码（房东登录必填）
     */
    private String password;
    
    /**
     * 验证码（租客登录使用）
     */
    private String code;
    
    /**
     * 登录类型：1-房东 2-租客
     */
    private Integer loginType;
}