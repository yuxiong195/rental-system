package com.rental.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bills")
public class Bill {
    
    /**
     * 账单ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 账单编号，唯一标识
     */
    private String billNo;
    
    /**
     * 房间ID，关联rooms表
     */
    private Long roomId;
    
    /**
     * 租客ID，关联users表
     */
    private Long tenantId;
    
    /**
     * 账单月份（YYYY-MM）
     */
    private String billMonth;
    
    /**
     * 租金金额（元）
     */
    private BigDecimal rentAmount;
    
    /**
     * 水费金额（元）
     */
    private BigDecimal waterAmount;
    
    /**
     * 电费金额（元）
     */
    private BigDecimal electricityAmount;
    
    /**
     * 卫生费金额（元）
     */
    private BigDecimal cleaningAmount;
    
    /**
     * 其他费用明细 JSON格式 [{name, amount}]
     */
    private String otherDetails;
    
    /**
     * 总金额（元）
     */
    private BigDecimal totalAmount;
    
    /**
     * 关联的抄表记录ID
     */
    private Long meterReadingId;
    
    /**
     * 状态：1-待支付 2-已支付 3-已作废
     */
    private Integer status;
    
    /**
     * 已付金额（元）
     */
    private BigDecimal paidAmount;
    
    /**
     * 支付时间
     */
    private LocalDateTime paidAt;
    
    /**
     * 支付方式：cash/wechat/alipay/bank
     */
    private String paymentMethod;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}