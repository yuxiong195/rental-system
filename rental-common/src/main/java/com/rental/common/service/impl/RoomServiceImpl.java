package com.rental.common.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.dto.RoomDTO;
import com.rental.common.entity.Room;
import com.rental.common.entity.User;
import com.rental.common.enums.ResultCode;
import com.rental.common.exception.BusinessException;
import com.rental.common.mapper.PropertyMapper;
import com.rental.common.mapper.RoomMapper;
import com.rental.common.mapper.UserMapper;
import com.rental.common.service.RoomService;
import com.rental.common.vo.RoomVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间管理服务实现类
 */
@Slf4j
@Service
public class RoomServiceImpl implements RoomService {
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private PropertyMapper propertyMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public IPage<RoomVO> getRoomPage(Long current, Long size, Long landlordId, Integer status, String keyword) {
        Page<RoomVO> page = new Page<>(current, size);
        return roomMapper.selectRoomPage(page, landlordId, status, keyword);
    }
    
    @Override
    public RoomVO getRoomById(Long roomId, Long landlordId) {
        // 验证房间是否属于该房东
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        // 验证权限
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该房间");
        }
        
        // 转换为VO
        RoomVO roomVO = new RoomVO();
        BeanUtils.copyProperties(room, roomVO);
        
        // 设置状态文本
        roomVO.setStatusText(getRoomStatusText(room.getStatus()));
        
        return roomVO;
    }
    
    @Override
    @Transactional
    public Long addRoom(RoomDTO roomDTO, Long landlordId) {
        // 验证房产权限
        if (propertyMapper.countByIdAndLandlordId(roomDTO.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权在该房产下添加房间");
        }
        
        // 检查房间名称是否重复
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("property_id", roomDTO.getPropertyId())
               .eq("room_name", roomDTO.getRoomName());
        if (roomMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, "房间名称已存在");
        }
        
        // 创建房间
        Room room = new Room();
        BeanUtils.copyProperties(roomDTO, room);
        room.setStatus(1); // 默认空置状态
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        
        roomMapper.insert(room);
        log.info("房东{}添加房间成功，房间ID：{}", landlordId, room.getId());
        
        return room.getId();
    }
    
    @Override
    @Transactional
    public boolean updateRoom(RoomDTO roomDTO, Long landlordId) {
        // 验证房间权限
        Room existRoom = roomMapper.selectById(roomDTO.getId());
        if (existRoom == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(existRoom.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该房间");
        }
        
        // 检查房间名称是否重复（排除自身）
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        wrapper.eq("property_id", existRoom.getPropertyId())
               .eq("room_name", roomDTO.getRoomName())
               .ne("id", roomDTO.getId());
        if (roomMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, "房间名称已存在");
        }
        
        // 更新房间信息
        Room room = new Room();
        BeanUtils.copyProperties(roomDTO, room);
        room.setUpdatedAt(LocalDateTime.now());
        
        int result = roomMapper.updateById(room);
        log.info("房东{}更新房间成功，房间ID：{}", landlordId, room.getId());
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteRoom(Long roomId, Long landlordId) {
        // 验证房间权限
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除该房间");
        }
        
        // 检查房间是否有租客
        if (room.getTenantId() != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "房间已出租，无法删除");
        }
        
        int result = roomMapper.deleteById(roomId);
        log.info("房东{}删除房间成功，房间ID：{}", landlordId, roomId);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean bindTenant(Long roomId, String tenantPhone, String rentStartDate, Long landlordId) {
        // 验证房间权限
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该房间");
        }
        
        // 检查房间状态
        if (room.getStatus() == 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "房间已出租");
        }
        
        // 查找或创建租客
        User tenant = userMapper.findByPhone(tenantPhone);
        if (tenant == null) {
            // 自动创建租客账号
            tenant = new User();
            tenant.setPhone(tenantPhone);
            tenant.setName("租客" + tenantPhone.substring(7));
            tenant.setUserType(2); // 租客
            tenant.setStatus(1);
            tenant.setCreatedAt(LocalDateTime.now());
            tenant.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(tenant);
            log.info("自动创建租客账号，手机号：{}", tenantPhone);
        } else if (tenant.getUserType() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该手机号已被房东占用");
        }
        
        // 绑定租客
        int result = roomMapper.bindTenant(roomId, tenant.getId(), tenantPhone, rentStartDate);
        log.info("房东{}绑定租客成功，房间ID：{}, 租客手机号：{}", landlordId, roomId, tenantPhone);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean unbindTenant(Long roomId, Long landlordId) {
        // 验证房间权限
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该房间");
        }
        
        // 检查房间状态
        if (room.getStatus() != 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "房间未出租");
        }
        
        // 解绑租客
        int result = roomMapper.unbindTenant(roomId);
        log.info("房东{}解绑租客成功，房间ID：{}", landlordId, roomId);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean updateRoomFees(RoomDTO roomDTO, Long landlordId) {
        // 验证房间权限
        Room room = roomMapper.selectById(roomDTO.getId());
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改该房间费用");
        }
        
        // 只更新费用相关字段
        Room updateRoom = new Room();
        updateRoom.setId(roomDTO.getId());
        updateRoom.setMonthlyRent(roomDTO.getMonthlyRent());
        updateRoom.setCleaningFee(roomDTO.getCleaningFee());
        updateRoom.setWaterPrice(roomDTO.getWaterPrice());
        updateRoom.setElectricityPrice(roomDTO.getElectricityPrice());
        updateRoom.setOtherFees(roomDTO.getOtherFees());
        updateRoom.setUpdatedAt(LocalDateTime.now());
        
        int result = roomMapper.updateById(updateRoom);
        log.info("房东{}更新房间费用成功，房间ID：{}", landlordId, roomDTO.getId());
        
        return result > 0;
    }
    
    @Override
    public List<Room> getLandlordRooms(Long landlordId) {
        return roomMapper.selectByLandlordId(landlordId);
    }
    
    @Override
    @Transactional
    public int batchUpdateStatus(List<Long> roomIds, Integer status, Long landlordId) {
        int updateCount = 0;
        
        for (Long roomId : roomIds) {
            // 验证权限
            Room room = roomMapper.selectById(roomId);
            if (room != null && propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) > 0) {
                Room updateRoom = new Room();
                updateRoom.setId(roomId);
                updateRoom.setStatus(status);
                updateRoom.setUpdatedAt(LocalDateTime.now());
                
                if (roomMapper.updateById(updateRoom) > 0) {
                    updateCount++;
                }
            }
        }
        
        log.info("房东{}批量更新房间状态，更新数量：{}", landlordId, updateCount);
        return updateCount;
    }
    
    /**
     * 获取房间状态文本
     */
    private String getRoomStatusText(Integer status) {
        switch (status) {
            case 1: return "空置";
            case 2: return "已出租";
            case 3: return "维修中";
            default: return "未知";
        }
    }
}