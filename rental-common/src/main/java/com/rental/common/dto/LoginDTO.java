package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求参数")
public class LoginDTO {
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码（房东登录必填）
     */
    @Schema(description = "密码（房东登录时必填）", example = "password123")
    private String password;
    
    /**
     * 验证码（租客登录使用）
     */
    @Schema(description = "短信验证码（租客登录时必填）", example = "123456")
    private String code;
    
    /**
     * 登录类型：1-房东 2-租客
     */
    @Schema(description = "登录类型", example = "1", allowableValues = {"1", "2"})
    private Integer loginType;
}