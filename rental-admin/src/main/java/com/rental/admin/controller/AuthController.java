package com.rental.admin.controller;

import com.rental.common.dto.LoginDTO;
import com.rental.common.dto.RegisterDTO;
import com.rental.common.result.Result;
import com.rental.common.service.AuthService;
import com.rental.common.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

/**
 * 🔐 认证控制器
 * 提供房东注册/登录、租客登录、验证码管理等功能
 */
@Slf4j
@Tag(name = "🔐 认证管理", description = "用户认证相关接口，包括注册、登录、验证码等")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 房东注册
     */
    @Operation(
        summary = "房东注册", 
        description = "房东用户注册，需要手机号、密码和短信验证码"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "注册成功", 
            content = @Content(schema = @Schema(implementation = Result.class))),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "500", description = "注册失败")
    })
    @PostMapping("/register")
    public Result<LoginVO> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "注册信息",
                content = @Content(
                    schema = @Schema(implementation = RegisterDTO.class),
                    examples = @ExampleObject(
                        name = "注册示例",
                        value = """
                        {
                          "phone": "13800138000",
                          "password": "password123",
                          "confirmPassword": "password123", 
                          "name": "张房东",
                          "code": "123456"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody RegisterDTO registerDTO) {
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success("注册成功", loginVO);
    }
    
    /**
     * 用户登录（房东/租客）
     */
    @Operation(
        summary = "用户登录", 
        description = """
        支持两种登录方式：
        - 房东登录：loginType=1，需要 phone + password
        - 租客登录：loginType=2，需要 phone + code（短信验证码）
        """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "401", description = "认证失败"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/login")
    public Result<LoginVO> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "登录信息",
                content = @Content(
                    schema = @Schema(implementation = LoginDTO.class),
                    examples = {
                        @ExampleObject(
                            name = "房东登录",
                            description = "房东使用密码登录",
                            value = """
                            {
                              "phone": "13800138000",
                              "password": "password123",
                              "loginType": 1
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "租客登录",
                            description = "租客使用验证码登录",
                            value = """
                            {
                              "phone": "13900139000",
                              "code": "123456",
                              "loginType": 2
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }
    
    /**
     * 发送短信验证码
     */
    @Operation(
        summary = "发送验证码", 
        description = "发送短信验证码，支持注册和登录两种场景，验证码5分钟内有效"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "发送成功"),
        @ApiResponse(responseCode = "400", description = "手机号格式错误"),
        @ApiResponse(responseCode = "429", description = "发送频率过快")
    })
    @PostMapping("/sms/send")
    public Result<Boolean> sendSmsCode(
            @Parameter(description = "手机号", example = "13800138000", required = true)
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String phone,
            
            @Parameter(description = "验证码类型", example = "register", required = true)
            @Pattern(regexp = "^(register|login)$", message = "类型只能是register或login")
            @RequestParam String type) {
        
        boolean success = authService.sendSmsCode(phone, type);
        return Result.success("验证码发送成功，请注意查收", success);
    }
    
    /**
     * 验证短信验证码
     */
    @Operation(
        summary = "验证验证码", 
        description = "验证短信验证码是否正确，主要用于前端实时校验"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "验证成功"),
        @ApiResponse(responseCode = "400", description = "验证码错误或已过期")
    })
    @PostMapping("/sms/verify")
    public Result<Boolean> verifySmsCode(
            @Parameter(description = "手机号", example = "13800138000", required = true)
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String phone,
            
            @Parameter(description = "验证码", example = "123456", required = true)
            @RequestParam String code) {
        
        boolean valid = authService.verifySmsCode(phone, code);
        return Result.success("验证成功", valid);
    }
}