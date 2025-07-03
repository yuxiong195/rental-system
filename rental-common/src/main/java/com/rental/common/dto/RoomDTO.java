package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 房间操作DTO
 */
@Data
public class RoomDTO {
    
    /**
     * 房间ID（编辑时使用）
     */
    private Long id;
    
    /**
     * 房产ID
     */
    @NotNull(message = "房产ID不能为空")
    private Long propertyId;
    
    /**
     * 房间名称/编号
     */
    @NotBlank(message = "房间名称不能为空")
    private String roomName;
    
    /**
     * 租客手机号（绑定租客时使用）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String tenantPhone;
    
    /**
     * 起租日期
     */
    private LocalDate rentStartDate;
    
    /**
     * 月租金（元）
     */
    @DecimalMin(value = "0.01", message = "月租金必须大于0")
    private BigDecimal monthlyRent;
    
    /**
     * 卫生费/月（元）
     */
    @DecimalMin(value = "0", message = "卫生费不能为负数")
    private BigDecimal cleaningFee;
    
    /**
     * 水费单价（元/吨）
     */
    @DecimalMin(value = "0", message = "水费单价不能为负数")
    private BigDecimal waterPrice;
    
    /**
     * 电费单价（元/度）
     */
    @DecimalMin(value = "0", message = "电费单价不能为负数")
    private BigDecimal electricityPrice;
    
    /**
     * 其他固定费用 JSON格式
     */
    private String otherFees;
    
    /**
     * 上次水表读数
     */
    @DecimalMin(value = "0", message = "水表读数不能为负数")
    private BigDecimal lastWaterReading;
    
    /**
     * 上次电表读数
     */
    @DecimalMin(value = "0", message = "电表读数不能为负数")
    private BigDecimal lastElectricityReading;
    
    /**
     * 备注
     */
    private String remark;
}