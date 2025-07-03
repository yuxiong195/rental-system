package com.rental.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单操作DTO
 */
@Data
@Schema(description = "账单操作请求参数")
public class BillDTO {
    
    /**
     * 账单ID（编辑时使用）
     */
    @Schema(description = "账单ID", example = "1")
    private Long id;
    
    /**
     * 房间ID
     */
    @Schema(description = "房间ID", example = "1", required = true)
    @NotNull(message = "房间ID不能为空")
    private Long roomId;
    
    /**
     * 账单月份
     */
    @Schema(description = "账单月份", example = "2024-07", required = true)
    @NotBlank(message = "账单月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "账单月份格式不正确，应为YYYY-MM")
    private String billMonth;
    
    /**
     * 抄表记录ID
     */
    @Schema(description = "抄表记录ID", example = "1")
    private Long meterReadingId;
    
    /**
     * 租金金额（手动调整时使用）
     */
    @Schema(description = "租金金额", example = "1500.00")
    @DecimalMin(value = "0", message = "租金金额不能为负数")
    private BigDecimal rentAmount;
    
    /**
     * 其他费用调整
     */
    @Schema(description = "其他费用调整项", example = "[{\"name\":\"管理费\",\"amount\":50.00}]")
    private String otherDetails;
    
    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "本月水电费较高")
    private String remark;
}

/**
 * 批量生成账单DTO
 */
@Data
@Schema(description = "批量生成账单请求参数")
class BatchBillGenerateDTO {
    
    /**
     * 账单月份
     */
    @Schema(description = "账单月份", example = "2024-07", required = true)
    @NotBlank(message = "账单月份不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "账单月份格式不正确，应为YYYY-MM")
    private String billMonth;
    
    /**
     * 房间ID列表（可选，不传则为所有已抄表房间）
     */
    @Schema(description = "房间ID列表", example = "[1,2,3]")
    private List<Long> roomIds;
    
    /**
     * 是否覆盖已存在的账单
     */
    @Schema(description = "是否覆盖已存在的账单", example = "false")
    private Boolean overwrite = false;
}