package com.rental.common.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.rental.common.dto.LoginDTO;
import com.rental.common.dto.RegisterDTO;
import com.rental.common.entity.User;
import com.rental.common.enums.ResultCode;
import com.rental.common.exception.BusinessException;
import com.rental.common.mapper.UserMapper;
import com.rental.common.service.AuthService;
import com.rental.common.utils.JwtUtil;
import com.rental.common.vo.LoginVO;
import com.rental.common.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    /**
     * 短信验证码Redis键前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";
    
    /**
     * 验证码有效期（分钟）
     */
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    
    @Override
    @Transactional
    public LoginVO register(RegisterDTO registerDTO) {
        // 校验密码一致性
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }
        
        // 验证短信验证码
        if (!verifySmsCode(registerDTO.getPhone(), registerDTO.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }
        
        // 检查手机号是否已注册
        User existUser = userMapper.findByPhone(registerDTO.getPhone());
        if (existUser != null) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, "手机号已被注册");
        }
        
        // 创建房东用户
        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setName(registerDTO.getName());
        user.setUserType(1); // 1-房东
        user.setStatus(1); // 1-正常
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("房东注册成功，用户ID：{}, 手机号：{}", user.getId(), user.getPhone());
        
        // 生成登录信息
        return generateLoginInfo(user);
    }
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userMapper.findByPhone(loginDTO.getPhone());
        
        if (loginDTO.getLoginType() == 1) {
            // 房东登录，需要密码
            return landlordLogin(user, loginDTO);
        } else {
            // 租客登录，需要验证码
            return tenantLogin(user, loginDTO);
        }
    }
    
    /**
     * 房东登录
     */
    private LoginVO landlordLogin(User user, LoginDTO loginDTO) {
        if (user == null || user.getUserType() != 1) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "房东账号不存在");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "密码错误");
        }
        
        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());
        
        log.info("房东登录成功，用户ID：{}, 手机号：{}", user.getId(), user.getPhone());
        return generateLoginInfo(user);
    }
    
    /**
     * 租客登录
     */
    private LoginVO tenantLogin(User user, LoginDTO loginDTO) {
        // 验证短信验证码
        if (!verifySmsCode(loginDTO.getPhone(), loginDTO.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "验证码错误或已过期");
        }
        
        if (user == null) {
            // 租客不存在，自动创建
            user = createTenantUser(loginDTO.getPhone());
        } else if (user.getUserType() != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该手机号已被房东占用");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        
        // 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId());
        
        log.info("租客登录成功，用户ID：{}, 手机号：{}", user.getId(), user.getPhone());
        return generateLoginInfo(user);
    }
    
    /**
     * 创建租客用户
     */
    private User createTenantUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setName("租客" + phone.substring(7)); // 默认姓名
        user.setUserType(2); // 2-租客
        user.setStatus(1); // 1-正常
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        log.info("自动创建租客用户，用户ID：{}, 手机号：{}", user.getId(), user.getPhone());
        
        return user;
    }
    
    /**
     * 生成登录信息
     */
    private LoginVO generateLoginInfo(User user) {
        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getUserType());
        
        // 构建用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userVO);
        loginVO.setExpireTime(System.currentTimeMillis() + 86400 * 1000); // 24小时后过期
        
        return loginVO;
    }
    
    @Override
    public boolean sendSmsCode(String phone, String type) {
        // 生成6位数字验证码
        String code = RandomUtil.randomNumbers(6);
        
        // 存储到Redis，5分钟过期
        String key = SMS_CODE_PREFIX + phone + ":" + type;
        redisTemplate.opsForValue().set(key, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // TODO: 集成短信服务商发送短信
        // 这里只是模拟发送，实际项目中需要集成阿里云、腾讯云等短信服务
        log.info("发送短信验证码，手机号：{}, 类型：{}, 验证码：{}", phone, type, code);
        
        return true;
    }
    
    @Override
    public boolean verifySmsCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone + ":*";
        
        // 获取所有相关的验证码
        for (String codeKey : redisTemplate.keys(key)) {
            String storedCode = redisTemplate.opsForValue().get(codeKey);
            if (code.equals(storedCode)) {
                // 验证成功，删除验证码
                redisTemplate.delete(codeKey);
                return true;
            }
        }
        
        return false;
    }
}