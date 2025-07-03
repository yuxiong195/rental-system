package com.rental.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("receipts")
public class Receipt {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String receiptNo;
    
    private Long billId;
    
    private BigDecimal amount;
    
    private String paymentMethod;
    
    private String transactionNo;
    
    private String payerName;
    
    private LocalDate receiptDate;
    
    private String remark;
    
    private LocalDateTime createdAt;
}