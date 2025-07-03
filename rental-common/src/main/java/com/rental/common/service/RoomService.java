package com.rental.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.RoomDTO;
import com.rental.common.entity.Room;
import com.rental.common.vo.RoomVO;

import java.util.List;

/**
 * 房间管理服务接口
 */
public interface RoomService {
    
    /**
     * 分页查询房间列表
     * @param current 当前页
     * @param size 每页大小
     * @param landlordId 房东ID
     * @param status 房间状态
     * @param keyword 搜索关键词
     * @return 房间分页数据
     */
    IPage<RoomVO> getRoomPage(Long current, Long size, Long landlordId, Integer status, String keyword);
    
    /**
     * 根据ID查询房间详情
     * @param roomId 房间ID
     * @param landlordId 房东ID（权限验证）
     * @return 房间信息
     */
    RoomVO getRoomById(Long roomId, Long landlordId);
    
    /**
     * 添加房间
     * @param roomDTO 房间信息
     * @param landlordId 房东ID
     * @return 房间ID
     */
    Long addRoom(RoomDTO roomDTO, Long landlordId);
    
    /**
     * 更新房间信息
     * @param roomDTO 房间信息
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean updateRoom(RoomDTO roomDTO, Long landlordId);
    
    /**
     * 删除房间
     * @param roomId 房间ID
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean deleteRoom(Long roomId, Long landlordId);
    
    /**
     * 绑定租客到房间
     * @param roomId 房间ID
     * @param tenantPhone 租客手机号
     * @param rentStartDate 起租日期
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean bindTenant(Long roomId, String tenantPhone, String rentStartDate, Long landlordId);
    
    /**
     * 解绑租客
     * @param roomId 房间ID
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean unbindTenant(Long roomId, Long landlordId);
    
    /**
     * 更新房间费用标准
     * @param roomDTO 房间费用信息
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean updateRoomFees(RoomDTO roomDTO, Long landlordId);
    
    /**
     * 获取房东的所有房间（用于下拉选择）
     * @param landlordId 房东ID
     * @return 房间列表
     */
    List<Room> getLandlordRooms(Long landlordId);
    
    /**
     * 批量更新房间状态
     * @param roomIds 房间ID列表
     * @param status 新状态
     * @param landlordId 房东ID（权限验证）
     * @return 更新数量
     */
    int batchUpdateStatus(List<Long> roomIds, Integer status, Long landlordId);
}