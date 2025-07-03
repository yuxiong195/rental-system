package com.rental.common.config;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rental.common.result.Result;
import com.rental.common.enums.ResultCode;
import com.rental.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getTokenFromRequest(request);
        
        if (token == null) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED.getCode(), "未提供认证token");
            return false;
        }
        
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String phone = jwtUtil.getPhoneFromToken(token);
            Integer userType = jwtUtil.getUserTypeFromToken(token);
            
            request.setAttribute("userId", userId);
            request.setAttribute("phone", phone);
            request.setAttribute("userType", userType);
            
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            writeErrorResponse(response, ResultCode.INVALID_TOKEN.getCode(), "无效的token");
            return false;
        }
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return request.getParameter("token");
    }
    
    private void writeErrorResponse(HttpServletResponse response, Integer code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Result<?> result = Result.error(code, message);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(result));
            writer.flush();
        }
    }
}