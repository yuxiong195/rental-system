package com.rental.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.RoomDTO;
import com.rental.common.entity.Room;
import com.rental.common.result.Result;
import com.rental.common.service.RoomService;
import com.rental.common.vo.RoomVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 房间管理控制器
 */
@Slf4j
@Tag(name = "房间管理", description = "房间增删改查、费用管理、租客绑定")
@RestController
@RequestMapping("/rooms")
@Validated
public class RoomController {
    
    @Autowired
    private RoomService roomService;
    
    /**
     * 分页查询房间列表
     */
    @Operation(summary = "房间列表", description = "分页查询房东的房间列表，支持状态筛选和关键词搜索")
    @GetMapping("/page")
    public Result<IPage<RoomVO>> getRoomPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        
        // 从JWT中获取房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        
        IPage<RoomVO> page = roomService.getRoomPage(current, size, landlordId, status, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取房间详情
     */
    @Operation(summary = "房间详情", description = "根据ID查询房间详细信息")
    @GetMapping("/{id}")
    public Result<RoomVO> getRoomById(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        RoomVO roomVO = roomService.getRoomById(id, landlordId);
        return Result.success(roomVO);
    }
    
    /**
     * 添加房间
     */
    @Operation(summary = "添加房间", description = "在指定房产下添加新房间")
    @PostMapping
    public Result<Long> addRoom(@Valid @RequestBody RoomDTO roomDTO, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        Long roomId = roomService.addRoom(roomDTO, landlordId);
        return Result.success("房间添加成功", roomId);
    }
    
    /**
     * 更新房间信息
     */
    @Operation(summary = "更新房间", description = "更新房间基本信息")
    @PutMapping
    public Result<Boolean> updateRoom(@Valid @RequestBody RoomDTO roomDTO, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = roomService.updateRoom(roomDTO, landlordId);
        return Result.success("房间更新成功", success);
    }
    
    /**
     * 删除房间
     */
    @Operation(summary = "删除房间", description = "删除指定房间（仅限空置房间）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRoom(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = roomService.deleteRoom(id, landlordId);
        return Result.success("房间删除成功", success);
    }
    
    /**
     * 绑定租客
     */
    @Operation(summary = "绑定租客", description = "将租客绑定到指定房间")
    @PostMapping("/{id}/bind-tenant")
    public Result<Boolean> bindTenant(
            @PathVariable Long id,
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            @RequestParam String tenantPhone,
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式不正确，应为YYYY-MM-DD")
            @RequestParam String rentStartDate,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = roomService.bindTenant(id, tenantPhone, rentStartDate, landlordId);
        return Result.success("租客绑定成功", success);
    }
    
    /**
     * 解绑租客
     */
    @Operation(summary = "解绑租客", description = "解除房间与租客的绑定关系")
    @PostMapping("/{id}/unbind-tenant")
    public Result<Boolean> unbindTenant(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = roomService.unbindTenant(id, landlordId);
        return Result.success("租客解绑成功", success);
    }
    
    /**
     * 更新房间费用标准
     */
    @Operation(summary = "更新费用标准", description = "更新房间的租金和各项费用标准")
    @PutMapping("/fees")
    public Result<Boolean> updateRoomFees(@Valid @RequestBody RoomDTO roomDTO, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        boolean success = roomService.updateRoomFees(roomDTO, landlordId);
        return Result.success("费用标准更新成功", success);
    }
    
    /**
     * 获取房东的所有房间（用于下拉选择）
     */
    @Operation(summary = "房间选项", description = "获取房东所有房间列表，用于下拉选择")
    @GetMapping("/options")
    public Result<List<Room>> getLandlordRooms(HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        List<Room> rooms = roomService.getLandlordRooms(landlordId);
        return Result.success(rooms);
    }
    
    /**
     * 批量更新房间状态
     */
    @Operation(summary = "批量更新状态", description = "批量更新多个房间的状态")
    @PutMapping("/batch-status")
    public Result<Integer> batchUpdateStatus(
            @RequestBody List<Long> roomIds,
            @NotNull(message = "状态不能为空")
            @RequestParam Integer status,
            HttpServletRequest request) {
        
        Long landlordId = (Long) request.getAttribute("userId");
        int updateCount = roomService.batchUpdateStatus(roomIds, status, landlordId);
        return Result.success("批量更新成功", updateCount);
    }
}