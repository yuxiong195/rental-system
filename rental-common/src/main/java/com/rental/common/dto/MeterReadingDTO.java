package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 抄表记录DTO
 */
@Data
public class MeterReadingDTO {
    
    /**
     * 抄表记录ID（编辑时使用）
     */
    private Long id;
    
    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空")
    private Long roomId;
    
    /**
     * 抄表月份（YYYY-MM）
     */
    @NotBlank(message = "抄表月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "抄表月份格式不正确，应为YYYY-MM")
    private String readingMonth;
    
    /**
     * 本期水表读数
     */
    @NotNull(message = "水表读数不能为空")
    @DecimalMin(value = "0", message = "水表读数不能为负数")
    private BigDecimal waterReading;
    
    /**
     * 本期电表读数
     */
    @NotNull(message = "电表读数不能为空")
    @DecimalMin(value = "0", message = "电表读数不能为负数")
    private BigDecimal electricityReading;
    
    /**
     * 抄表日期
     */
    private LocalDate readingDate;
    
    /**
     * 水电表照片URL列表
     */
    private List<String> imageUrls;
}

/**
 * 批量抄表DTO
 */
@Data
class BatchMeterReadingDTO {
    
    /**
     * 抄表月份（YYYY-MM）
     */
    @NotBlank(message = "抄表月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "抄表月份格式不正确，应为YYYY-MM")
    private String readingMonth;
    
    /**
     * 抄表日期
     */
    private LocalDate readingDate;
    
    /**
     * 抄表记录列表
     */
    @NotNull(message = "抄表记录列表不能为空")
    private List<MeterReadingItem> readings;
    
    /**
     * 单条抄表记录项
     */
    @Data
    public static class MeterReadingItem {
        
        /**
         * 房间ID
         */
        @NotNull(message = "房间ID不能为空")
        private Long roomId;
        
        /**
         * 本期水表读数
         */
        @NotNull(message = "水表读数不能为空")
        @DecimalMin(value = "0", message = "水表读数不能为负数")
        private BigDecimal waterReading;
        
        /**
         * 本期电表读数
         */
        @NotNull(message = "电表读数不能为空")
        @DecimalMin(value = "0", message = "电表读数不能为负数")
        private BigDecimal electricityReading;
        
        /**
         * 水电表照片URL列表
         */
        private List<String> imageUrls;
    }
}