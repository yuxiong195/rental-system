package com.rental.common.enums;

import lombok.Getter;

@Getter
public enum ResultCode {
    
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    
    INVALID_TOKEN(4001, "无效token"),
    TOKEN_EXPIRED(4002, "token已过期"),
    INVALID_PHONE(4003, "手机号格式错误"),
    USER_NOT_FOUND(4004, "用户不存在"),
    ROOM_NOT_FOUND(4005, "房间不存在"),
    BILL_NOT_FOUND(4006, "账单不存在"),
    
    PARAM_ERROR(5001, "参数错误"),
    DUPLICATE_DATA(5002, "数据重复"),
    
    SYSTEM_ERROR(9999, "系统错误");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}