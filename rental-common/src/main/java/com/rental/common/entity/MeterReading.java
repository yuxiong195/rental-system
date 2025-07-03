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
@TableName("meter_readings")
public class MeterReading {
    
    /**
     * 抄表记录ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 房间ID，关联rooms表
     */
    private Long roomId;
    
    /**
     * 抄表月份（YYYY-MM）
     */
    private String readingMonth;
    
    /**
     * 本期水表读数
     */
    private BigDecimal waterReading;
    
    /**
     * 本期电表读数
     */
    private BigDecimal electricityReading;
    
    /**
     * 上期水表读数（冗余存储）
     */
    private BigDecimal prevWaterReading;
    
    /**
     * 上期电表读数（冗余存储）
     */
    private BigDecimal prevElectricityReading;
    
    /**
     * 用水量（自动计算）
     */
    private BigDecimal waterUsage;
    
    /**
     * 用电量（自动计算）
     */
    private BigDecimal electricityUsage;
    
    /**
     * 抄表日期
     */
    private LocalDate readingDate;
    
    /**
     * 水电表照片 JSON格式 [{type, url}]
     */
    private String images;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}