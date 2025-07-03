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
@TableName("fee_templates")
public class FeeTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long landlordId;
    
    private String name;
    
    private BigDecimal monthlyRent;
    
    private BigDecimal cleaningFee;
    
    private BigDecimal waterPrice;
    
    private BigDecimal electricityPrice;
    
    private String otherFees;
    
    private Integer isDefault;
    
    private LocalDateTime createdAt;
}