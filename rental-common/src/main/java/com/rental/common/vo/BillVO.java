package com.rental.common.vo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单信息VO
 */
@Data
@Schema(description = "账单信息响应")
public class BillVO {
    
    /**
     * 账单ID
     */
    @Schema(description = "账单ID", example = "1")
    private Long id;
    
    /**
     * 账单编号
     */
    @Schema(description = "账单编号", example = "B202407001")
    private String billNo;
    
    /**
     * 房间ID
     */
    @Schema(description = "房间ID", example = "1")
    private Long roomId;
    
    /**
     * 房间名称
     */
    @Schema(description = "房间名称", example = "A101")
    private String roomName;
    
    /**
     * 房产名称
     */
    @Schema(description = "房产名称", example = "阳光小区")
    private String propertyName;
    
    /**
     * 租客ID
     */
    @Schema(description = "租客ID", example = "2")
    private Long tenantId;
    
    /**
     * 租客姓名
     */
    @Schema(description = "租客姓名", example = "张三")
    private String tenantName;
    
    /**
     * 租客手机号
     */
    @Schema(description = "租客手机号", example = "13900139000")
    private String tenantPhone;
    
    /**
     * 账单月份
     */
    @Schema(description = "账单月份", example = "2024-07")
    private String billMonth;
    
    /**
     * 租金金额
     */
    @Schema(description = "租金金额", example = "1500.00")
    private BigDecimal rentAmount;
    
    /**
     * 水费金额
     */
    @Schema(description = "水费金额", example = "45.60")
    private BigDecimal waterAmount;
    
    /**
     * 电费金额
     */
    @Schema(description = "电费金额", example = "89.40")
    private BigDecimal electricityAmount;
    
    /**
     * 卫生费金额
     */
    @Schema(description = "卫生费金额", example = "30.00")
    private BigDecimal cleaningAmount;
    
    /**
     * 其他费用明细
     */
    @Schema(description = "其他费用明细", example = "[{\"name\":\"管理费\",\"amount\":50.00}]")
    private String otherDetails;
    
    /**
     * 总金额
     */
    @Schema(description = "总金额", example = "1715.00")
    private BigDecimal totalAmount;
    
    /**
     * 抄表记录ID
     */
    @Schema(description = "抄表记录ID", example = "1")
    private Long meterReadingId;
    
    /**
     * 用水量
     */
    @Schema(description = "用水量", example = "15.2")
    private BigDecimal waterUsage;
    
    /**
     * 用电量
     */
    @Schema(description = "用电量", example = "149.0")
    private BigDecimal electricityUsage;
    
    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    /**
     * 状态文本
     */
    @Schema(description = "状态文本", example = "待支付")
    private String statusText;
    
    /**
     * 已付金额
     */
    @Schema(description = "已付金额", example = "0.00")
    private BigDecimal paidAmount;
    
    /**
     * 支付时间
     */
    @Schema(description = "支付时间")
    private LocalDateTime paidAt;
    
    /**
     * 支付方式
     */
    @Schema(description = "支付方式", example = "wechat")
    private String paymentMethod;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}