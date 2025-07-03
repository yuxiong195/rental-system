package com.rental.admin.controller;

import com.rental.common.dto.LoginDTO;
import com.rental.common.dto.RegisterDTO;
import com.rental.common.result.Result;
import com.rental.common.service.AuthService;
import com.rental.common.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "认证管理", description = "房东认证相关接口")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 房东注册
     */
    @Operation(summary = "房东注册", description = "房东用户注册接口")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success("注册成功", loginVO);
    }
    
    /**
     * 用户登录（房东/租客）
     */
    @Operation(summary = "用户登录", description = "房东密码登录，租客验证码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }
    
    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送验证码", description = "发送短信验证码，用于注册或租客登录")
    @PostMapping("/sms/send")
    public Result<Boolean> sendSmsCode(
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String phone,
            @Pattern(regexp = "^(register|login)$", message = "类型只能是register或login")
            @RequestParam String type) {
        
        boolean success = authService.sendSmsCode(phone, type);
        return Result.success("验证码发送成功", success);
    }
    
    /**
     * 验证短信验证码
     */
    @Operation(summary = "验证验证码", description = "验证短信验证码是否正确")
    @PostMapping("/sms/verify")
    public Result<Boolean> verifySmsCode(
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String phone,
            @RequestParam String code) {
        
        boolean valid = authService.verifySmsCode(phone, code);
        return Result.success("验证成功", valid);
    }
}