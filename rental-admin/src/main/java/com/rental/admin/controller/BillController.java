package com.rental.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.BillDTO;
import com.rental.common.result.Result;
import com.rental.common.service.BillService;
import com.rental.common.vo.BillVO;
import com.rental.common.mapper.BillMapper.BillStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

/**
 * 💰 账单管理控制器
 * 提供账单CRUD、批量生成、支付管理等功能
 */
@Slf4j
@Tag(name = "💰 账单管理", description = "账单生成、查询、支付管理等功能")
@RestController
@RequestMapping("/bills")
@Validated
public class BillController {
    
    @Autowired
    private BillService billService;
    
    /**
     * 分页查询账单列表
     */
    @Operation(
        summary = "账单列表", 
        description = "分页查询房东的账单列表，支持月份、状态筛选和关键词搜索"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @GetMapping("/page")
    public Result<IPage<BillVO>> getBillPage(
            @Parameter(description = "当前页", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            
            @Parameter(description = "账单月份", example = "2024-07")
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam(required = false) String billMonth,
            
            @Parameter(description = "账单状态", example = "1")
            @RequestParam(required = false) Integer status,
            
            @Parameter(description = "搜索关键词", example = "A101")
            @RequestParam(required = false) String keyword,
            
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        
        IPage<BillVO> page = billService.getBillPage(current, size, landlordId, billMonth, status, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取账单详情
     */
    @Operation(summary = "账单详情", description = "根据ID查询账单详细信息")
    @GetMapping("/{id}")
    public Result<BillVO> getBillById(
            @Parameter(description = "账单ID", example = "1", required = true)
            @PathVariable Long id, 
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        BillVO billVO = billService.getBillById(id, landlordId);
        return Result.success(billVO);
    }
    
    /**
     * 基于抄表记录生成账单
     */
    @Operation(
        summary = "生成账单", 
        description = "基于指定的抄表记录自动计算并生成账单"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "400", description = "抄表记录不存在或已生成账单"),
        @ApiResponse(responseCode = "403", description = "无权操作")
    })
    @PostMapping("/generate/{meterReadingId}")
    public Result<Long> generateBill(
            @Parameter(description = "抄表记录ID", example = "1", required = true)
            @PathVariable Long meterReadingId,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        Long billId = billService.generateBillFromMeterReading(meterReadingId, landlordId);
        return Result.success("账单生成成功", billId);
    }
    
    /**
     * 手动创建账单
     */
    @Operation(summary = "创建账单", description = "手动创建账单，适用于无抄表记录的情况")
    @PostMapping
    public Result<Long> createBill(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "账单信息",
                content = @Content(
                    schema = @Schema(implementation = BillDTO.class),
                    examples = @ExampleObject(
                        name = "创建账单示例",
                        value = """
                        {
                          "roomId": 1,
                          "billMonth": "2024-07",
                          "rentAmount": 1500.00,
                          "otherDetails": "[{\\"name\\":\\"管理费\\",\\"amount\\":50.00}]",
                          "remark": "手动创建的账单"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody BillDTO billDTO,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        Long billId = billService.createBill(billDTO, landlordId);
        return Result.success("账单创建成功", billId);
    }
    
    /**
     * 更新账单信息
     */
    @Operation(summary = "更新账单", description = "更新待支付状态的账单信息")
    @PutMapping
    public Result<Boolean> updateBill(
            @Valid @RequestBody BillDTO billDTO,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = billService.updateBill(billDTO, landlordId);
        return Result.success("账单更新成功", success);
    }
    
    /**
     * 删除账单
     */
    @Operation(summary = "删除账单", description = "删除待支付状态的账单")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteBill(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = billService.deleteBill(id, landlordId);
        return Result.success("账单删除成功", success);
    }
    
    /**
     * 批量生成账单
     */
    @Operation(
        summary = "批量生成账单", 
        description = "批量生成指定月份的账单，基于已完成的抄表记录"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "批量生成完成"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/batch-generate")
    public Result<Integer> batchGenerateBills(
            @Parameter(description = "账单月份", example = "2024-07", required = true)
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam String billMonth,
            
            @Parameter(description = "房间ID列表（可选）", example = "[1,2,3]")
            @RequestParam(required = false) List<Long> roomIds,
            
            @Parameter(description = "是否覆盖已存在的账单", example = "false")
            @RequestParam(defaultValue = "false") Boolean overwrite,
            
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        int generatedCount = billService.batchGenerateBills(billMonth, roomIds, overwrite, landlordId);
        return Result.success("批量生成账单完成，成功生成 " + generatedCount + " 个账单", generatedCount);
    }
    
    /**
     * 标记账单为已支付
     */
    @Operation(
        summary = "标记已支付", 
        description = "标记账单为已支付状态，记录支付信息"
    )
    @PostMapping("/{id}/pay")
    public Result<Boolean> markBillAsPaid(
            @Parameter(description = "账单ID", example = "1", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "实付金额", example = "1715.00", required = true)
            @DecimalMin(value = "0.01", message = "支付金额必须大于0")
            @RequestParam BigDecimal paidAmount,
            
            @Parameter(description = "支付方式", example = "wechat", required = true)
            @RequestParam String paymentMethod,
            
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = billService.markBillAsPaid(id, paidAmount, paymentMethod, landlordId);
        return Result.success("账单支付状态更新成功", success);
    }
    
    /**
     * 作废账单
     */
    @Operation(summary = "作废账单", description = "将账单标记为作废状态")
    @PostMapping("/{id}/void")
    public Result<Boolean> voidBill(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = billService.voidBill(id, landlordId);
        return Result.success("账单作废成功", success);
    }
    
    /**
     * 获取账单统计信息
     */
    @Operation(
        summary = "账单统计", 
        description = "获取账单统计信息，包括总数、待支付数、已支付数、金额统计等"
    )
    @GetMapping("/statistics")
    public Result<BillStatistics> getBillStatistics(
            @Parameter(description = "账单月份（可选）", example = "2024-07")
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam(required = false) String billMonth,
            
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        BillStatistics statistics = billService.getBillStatistics(landlordId, billMonth);
        return Result.success(statistics);
    }
    
    /**
     * 获取指定月份的账单列表
     */
    @Operation(summary = "月度账单", description = "获取指定月份的所有账单")
    @GetMapping("/monthly/{billMonth}")
    public Result<List<BillVO>> getBillsByMonth(
            @Parameter(description = "账单月份", example = "2024-07", required = true)
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @PathVariable String billMonth,
            
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        List<BillVO> bills = billService.getBillsByMonth(landlordId, billMonth);
        return Result.success(bills);
    }
    
    /**
     * 检查账单是否存在
     */
    @Operation(summary = "检查账单存在", description = "检查指定房间和月份的账单是否已存在")
    @GetMapping("/exists")
    public Result<Boolean> existsBill(
            @Parameter(description = "房间ID", example = "1", required = true)
            @RequestParam Long roomId,
            
            @Parameter(description = "账单月份", example = "2024-07", required = true)
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam String billMonth) {
        
        boolean exists = billService.existsBill(roomId, billMonth);
        return Result.success(exists);
    }
}