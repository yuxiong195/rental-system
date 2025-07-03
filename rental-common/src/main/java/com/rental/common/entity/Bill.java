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
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String billNo;
    
    private Long roomId;
    
    private Long tenantId;
    
    private String billMonth;
    
    private BigDecimal rentAmount;
    
    private BigDecimal waterAmount;
    
    private BigDecimal electricityAmount;
    
    private BigDecimal cleaningAmount;
    
    private String otherDetails;
    
    private BigDecimal totalAmount;
    
    private Long meterReadingId;
    
    private Integer status;
    
    private BigDecimal paidAmount;
    
    private LocalDateTime paidAt;
    
    private String paymentMethod;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}