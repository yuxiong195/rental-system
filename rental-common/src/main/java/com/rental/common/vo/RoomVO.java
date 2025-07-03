package com.rental.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 房间信息VO
 */
@Data
public class RoomVO {
    
    /**
     * 房间ID
     */
    private Long id;
    
    /**
     * 房产ID
     */
    private Long propertyId;
    
    /**
     * 房产名称
     */
    private String propertyName;
    
    /**
     * 房间名称/编号
     */
    private String roomName;
    
    /**
     * 状态：1-空置 2-已出租 3-维修中
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 当前租客ID
     */
    private Long tenantId;
    
    /**
     * 租客手机号
     */
    private String tenantPhone;
    
    /**
     * 租客姓名
     */
    private String tenantName;
    
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
     * 其他固定费用
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