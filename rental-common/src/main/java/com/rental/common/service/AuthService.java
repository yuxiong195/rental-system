package com.rental.common.service;

import com.rental.common.dto.LoginDTO;
import com.rental.common.dto.RegisterDTO;
import com.rental.common.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 房东注册
     * @param registerDTO 注册信息
     * @return 登录结果
     */
    LoginVO register(RegisterDTO registerDTO);
    
    /**
     * 用户登录（房东/租客）
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);
    
    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param type 类型：register-注册，login-登录
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone, String type);
    
    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifySmsCode(String phone, String code);
}