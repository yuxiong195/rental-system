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
@TableName("rooms")
public class Room {
    
    /**
     * 房间ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 房产ID，关联properties表
     */
    private Long propertyId;
    
    /**
     * 房间名称/编号
     */
    private String roomName;
    
    /**
     * 状态：1-空置 2-已出租 3-维修中
     */
    private Integer status;
    
    /**
     * 当前租客ID
     */
    private Long tenantId;
    
    /**
     * 租客手机号，快速绑定用
     */
    private String tenantPhone;
    
    /**
     * 起租日期
     */
    private LocalDate rentStartDate;
    
    /**
     * 月租金（元）
     */
    private BigDecimal monthlyRent;
    
    /**
     * 卫生费/月（元）
     */
    private BigDecimal cleaningFee;
    
    /**
     * 水费单价（元/吨）
     */
    private BigDecimal waterPrice;
    
    /**
     * 电费单价（元/度）
     */
    private BigDecimal electricityPrice;
    
    /**
     * 其他固定费用 JSON格式 [{name, amount}]
     */
    private String otherFees;
    
    /**
     * 上次水表读数
     */
    private BigDecimal lastWaterReading;
    
    /**
     * 上次电表读数
     */
    private BigDecimal lastElectricityReading;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}