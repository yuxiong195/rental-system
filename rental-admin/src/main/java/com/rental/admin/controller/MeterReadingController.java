package com.rental.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.MeterReadingDTO;
import com.rental.common.entity.MeterReading;
import com.rental.common.result.Result;
import com.rental.common.service.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 📊 抄表记录管理控制器
 * 提供抄表记录CRUD、批量操作、统计查询等功能
 */
@Slf4j
@Tag(name = "📊 抄表管理", description = "抄表记录增删改查，支持批量操作、用量计算、月度统计")
@RestController
@RequestMapping("/meter-readings")
@Validated
public class MeterReadingController {
    
    @Autowired
    private MeterReadingService meterReadingService;
    
    /**
     * 分页查询抄表记录
     */
    @Operation(summary = "抄表记录列表", description = "分页查询抄表记录，支持月份和房间筛选")
    @GetMapping("/page")
    public Result<IPage<MeterReading>> getMeterReadingPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) 
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            String readingMonth,
            @RequestParam(required = false) Long roomId,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        
        IPage<MeterReading> page = meterReadingService.getMeterReadingPage(
                current, size, landlordId, readingMonth, roomId);
        return Result.success(page);
    }
    
    /**
     * 获取抄表记录详情
     */
    @Operation(summary = "抄表记录详情", description = "根据ID查询抄表记录详细信息")
    @GetMapping("/{id}")
    public Result<MeterReading> getMeterReadingById(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        MeterReading meterReading = meterReadingService.getMeterReadingById(id, landlordId);
        return Result.success(meterReading);
    }
    
    /**
     * 添加抄表记录
     */
    @Operation(summary = "添加抄表记录", description = "为指定房间添加抄表记录")
    @PostMapping
    public Result<Long> addMeterReading(@Valid @RequestBody MeterReadingDTO meterReadingDTO, 
                                       HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        Long readingId = meterReadingService.addMeterReading(meterReadingDTO, landlordId);
        return Result.success("抄表记录添加成功", readingId);
    }
    
    /**
     * 更新抄表记录
     */
    @Operation(summary = "更新抄表记录", description = "更新已有的抄表记录信息")
    @PutMapping
    public Result<Boolean> updateMeterReading(@Valid @RequestBody MeterReadingDTO meterReadingDTO,
                                             HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = meterReadingService.updateMeterReading(meterReadingDTO, landlordId);
        return Result.success("抄表记录更新成功", success);
    }
    
    /**
     * 删除抄表记录
     */
    @Operation(summary = "删除抄表记录", description = "删除指定的抄表记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMeterReading(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = meterReadingService.deleteMeterReading(id, landlordId);
        return Result.success("抄表记录删除成功", success);
    }
    
    /**
     * 批量添加抄表记录
     */
    @Operation(summary = "批量抄表", description = "批量添加多个房间的抄表记录")
    @PostMapping("/batch")
    public Result<Integer> batchAddMeterReadings(
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam String readingMonth,
            @Valid @RequestBody List<MeterReadingDTO> readings,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        int addCount = meterReadingService.batchAddMeterReadings(readingMonth, readings, landlordId);
        return Result.success("批量抄表完成", addCount);
    }
    
    /**
     * 获取房间最新抄表记录
     */
    @Operation(summary = "最新抄表记录", description = "获取指定房间的最新抄表记录")
    @GetMapping("/latest/{roomId}")
    public Result<MeterReading> getLatestMeterReading(@PathVariable Long roomId, 
                                                     HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        MeterReading meterReading = meterReadingService.getLatestMeterReading(roomId, landlordId);
        return Result.success(meterReading);
    }
    
    /**
     * 获取指定月份抄表统计
     */
    @Operation(summary = "月度抄表统计", description = "获取指定月份的抄表统计数据")
    @GetMapping("/monthly/{readingMonth}")
    public Result<List<MeterReading>> getMeterReadingsByMonth(
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @PathVariable String readingMonth,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        List<MeterReading> readings = meterReadingService.getMeterReadingsByMonth(landlordId, readingMonth);
        return Result.success(readings);
    }
    
    /**
     * 检查抄表记录是否存在
     */
    @Operation(summary = "检查记录存在", description = "检查指定房间和月份的抄表记录是否已存在")
    @GetMapping("/exists")
    public Result<Boolean> existsMeterReading(
            @RequestParam Long roomId,
            @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确，应为YYYY-MM")
            @RequestParam String readingMonth) {
        
        boolean exists = meterReadingService.existsMeterReading(roomId, readingMonth);
        return Result.success(exists);
    }
    
    /**
     * 基于抄表记录生成账单
     */
    @Operation(summary = "生成账单", description = "基于抄表记录自动生成租客账单")
    @PostMapping("/{id}/generate-bill")
    public Result<Long> generateBill(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        Long billId = meterReadingService.generateBill(id, landlordId);
        return Result.success("账单生成成功", billId);
    }
}