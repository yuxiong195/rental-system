package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 注册请求DTO
 */
@Data
@Schema(description = "房东注册请求参数")
public class RegisterDTO {
    
    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000", required = true)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @Schema(description = "密码（至少8位，包含字母和数字）", example = "password123", required = true)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$", 
             message = "密码至少8位，包含字母和数字")
    private String password;
    
    /**
     * 确认密码
     */
    @Schema(description = "确认密码", example = "password123", required = true)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    /**
     * 姓名
     */
    @Schema(description = "真实姓名", example = "张房东", required = true)
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    /**
     * 验证码
     */
    @Schema(description = "短信验证码", example = "123456", required = true)
    @NotBlank(message = "验证码不能为空")
    private String code;
}