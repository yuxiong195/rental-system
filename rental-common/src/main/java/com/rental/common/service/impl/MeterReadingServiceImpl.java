package com.rental.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.dto.MeterReadingDTO;
import com.rental.common.entity.MeterReading;
import com.rental.common.entity.Room;
import com.rental.common.enums.ResultCode;
import com.rental.common.exception.BusinessException;
import com.rental.common.mapper.MeterReadingMapper;
import com.rental.common.mapper.PropertyMapper;
import com.rental.common.mapper.RoomMapper;
import com.rental.common.service.MeterReadingService;
import com.rental.common.service.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 抄表记录服务实现类
 */
@Slf4j
@Service
public class MeterReadingServiceImpl implements MeterReadingService {
    
    @Autowired
    private MeterReadingMapper meterReadingMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private PropertyMapper propertyMapper;
    
    @Autowired
    private BillService billService;
    
    @Override
    public IPage<MeterReading> getMeterReadingPage(Long current, Long size, Long landlordId, 
                                                  String readingMonth, Long roomId) {
        Page<MeterReading> page = new Page<>(current, size);
        return meterReadingMapper.selectMeterReadingPage(page, landlordId, readingMonth, roomId);
    }
    
    @Override
    public MeterReading getMeterReadingById(Long readingId, Long landlordId) {
        MeterReading meterReading = meterReadingMapper.selectById(readingId);
        if (meterReading == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "抄表记录不存在");
        }
        
        // 验证权限：检查房间是否属于该房东
        validateRoomOwnership(meterReading.getRoomId(), landlordId);
        
        return meterReading;
    }
    
    @Override
    @Transactional
    public Long addMeterReading(MeterReadingDTO meterReadingDTO, Long landlordId) {
        // 验证房间权限
        validateRoomOwnership(meterReadingDTO.getRoomId(), landlordId);
        
        // 检查是否已存在该月份的抄表记录
        if (meterReadingMapper.countByRoomAndMonth(meterReadingDTO.getRoomId(), 
                                                  meterReadingDTO.getReadingMonth()) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, 
                    "该房间在" + meterReadingDTO.getReadingMonth() + "的抄表记录已存在");
        }
        
        // 获取上期读数
        MeterReading lastReading = meterReadingMapper.selectLatestByRoomId(meterReadingDTO.getRoomId());
        
        // 创建抄表记录
        MeterReading meterReading = new MeterReading();
        BeanUtils.copyProperties(meterReadingDTO, meterReading);
        
        // 设置上期读数
        if (lastReading != null) {
            meterReading.setPrevWaterReading(lastReading.getWaterReading());
            meterReading.setPrevElectricityReading(lastReading.getElectricityReading());
        } else {
            // 如果没有上期记录，从房间表获取初始读数
            Room room = roomMapper.selectById(meterReadingDTO.getRoomId());
            meterReading.setPrevWaterReading(room.getLastWaterReading() != null ? 
                    room.getLastWaterReading() : BigDecimal.ZERO);
            meterReading.setPrevElectricityReading(room.getLastElectricityReading() != null ? 
                    room.getLastElectricityReading() : BigDecimal.ZERO);
        }
        
        // 计算用量
        BigDecimal waterUsage = meterReading.getWaterReading().subtract(meterReading.getPrevWaterReading());
        BigDecimal electricityUsage = meterReading.getElectricityReading().subtract(meterReading.getPrevElectricityReading());
        
        // 验证用量合理性
        if (waterUsage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "水表读数不能小于上期读数");
        }
        if (electricityUsage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "电表读数不能小于上期读数");
        }
        
        meterReading.setWaterUsage(waterUsage);
        meterReading.setElectricityUsage(electricityUsage);
        
        // 处理图片
        if (CollUtil.isNotEmpty(meterReadingDTO.getImageUrls())) {
            meterReading.setImages(JSONUtil.toJsonStr(meterReadingDTO.getImageUrls()));
        }
        
        // 设置抄表日期
        if (meterReading.getReadingDate() == null) {
            meterReading.setReadingDate(LocalDate.now());
        }
        
        meterReading.setCreatedAt(LocalDateTime.now());
        
        meterReadingMapper.insert(meterReading);
        
        // 更新房间的最新读数（通过数据库触发器自动完成）
        log.info("房东{}添加抄表记录成功，记录ID：{}, 房间ID：{}, 月份：{}", 
                landlordId, meterReading.getId(), meterReading.getRoomId(), meterReading.getReadingMonth());
        
        return meterReading.getId();
    }
    
    @Override
    @Transactional
    public boolean updateMeterReading(MeterReadingDTO meterReadingDTO, Long landlordId) {
        // 验证记录存在性和权限
        MeterReading existReading = getMeterReadingById(meterReadingDTO.getId(), landlordId);
        
        // 验证房间权限
        validateRoomOwnership(meterReadingDTO.getRoomId(), landlordId);
        
        // 检查月份重复（排除自身）
        int count = meterReadingMapper.countByRoomAndMonth(meterReadingDTO.getRoomId(), 
                                                          meterReadingDTO.getReadingMonth());
        if (count > 0 && !existReading.getReadingMonth().equals(meterReadingDTO.getReadingMonth())) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, 
                    "该房间在" + meterReadingDTO.getReadingMonth() + "的抄表记录已存在");
        }
        
        // 重新计算用量
        BigDecimal waterUsage = meterReadingDTO.getWaterReading().subtract(existReading.getPrevWaterReading());
        BigDecimal electricityUsage = meterReadingDTO.getElectricityReading().subtract(existReading.getPrevElectricityReading());
        
        if (waterUsage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "水表读数不能小于上期读数");
        }
        if (electricityUsage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "电表读数不能小于上期读数");
        }
        
        // 更新记录
        MeterReading meterReading = new MeterReading();
        BeanUtils.copyProperties(meterReadingDTO, meterReading);
        meterReading.setWaterUsage(waterUsage);
        meterReading.setElectricityUsage(electricityUsage);
        
        if (CollUtil.isNotEmpty(meterReadingDTO.getImageUrls())) {
            meterReading.setImages(JSONUtil.toJsonStr(meterReadingDTO.getImageUrls()));
        }
        
        int result = meterReadingMapper.updateById(meterReading);
        log.info("房东{}更新抄表记录成功，记录ID：{}", landlordId, meterReading.getId());
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteMeterReading(Long readingId, Long landlordId) {
        // 验证记录存在性和权限
        MeterReading meterReading = getMeterReadingById(readingId, landlordId);
        
        // TODO: 检查是否已生成账单，如果已生成则不允许删除
        
        int result = meterReadingMapper.deleteById(readingId);
        log.info("房东{}删除抄表记录成功，记录ID：{}", landlordId, readingId);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public int batchAddMeterReadings(String readingMonth, List<MeterReadingDTO> readings, Long landlordId) {
        if (CollUtil.isEmpty(readings)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "抄表记录列表不能为空");
        }
        
        List<MeterReading> meterReadings = new ArrayList<>();
        
        for (MeterReadingDTO dto : readings) {
            // 验证房间权限
            validateRoomOwnership(dto.getRoomId(), landlordId);
            
            // 检查是否已存在
            if (meterReadingMapper.countByRoomAndMonth(dto.getRoomId(), readingMonth) > 0) {
                log.warn("房间{}在{}的抄表记录已存在，跳过", dto.getRoomId(), readingMonth);
                continue;
            }
            
            // 获取上期读数
            MeterReading lastReading = meterReadingMapper.selectLatestByRoomId(dto.getRoomId());
            
            MeterReading meterReading = new MeterReading();
            meterReading.setRoomId(dto.getRoomId());
            meterReading.setReadingMonth(readingMonth);
            meterReading.setWaterReading(dto.getWaterReading());
            meterReading.setElectricityReading(dto.getElectricityReading());
            meterReading.setReadingDate(dto.getReadingDate() != null ? dto.getReadingDate() : LocalDate.now());
            
            // 设置上期读数
            if (lastReading != null) {
                meterReading.setPrevWaterReading(lastReading.getWaterReading());
                meterReading.setPrevElectricityReading(lastReading.getElectricityReading());
            } else {
                Room room = roomMapper.selectById(dto.getRoomId());
                meterReading.setPrevWaterReading(room.getLastWaterReading() != null ? 
                        room.getLastWaterReading() : BigDecimal.ZERO);
                meterReading.setPrevElectricityReading(room.getLastElectricityReading() != null ? 
                        room.getLastElectricityReading() : BigDecimal.ZERO);
            }
            
            // 计算用量
            BigDecimal waterUsage = meterReading.getWaterReading().subtract(meterReading.getPrevWaterReading());
            BigDecimal electricityUsage = meterReading.getElectricityReading().subtract(meterReading.getPrevElectricityReading());
            
            if (waterUsage.compareTo(BigDecimal.ZERO) < 0 || electricityUsage.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("房间{}的读数异常，跳过", dto.getRoomId());
                continue;
            }
            
            meterReading.setWaterUsage(waterUsage);
            meterReading.setElectricityUsage(electricityUsage);
            
            if (CollUtil.isNotEmpty(dto.getImageUrls())) {
                meterReading.setImages(JSONUtil.toJsonStr(dto.getImageUrls()));
            }
            
            meterReading.setCreatedAt(LocalDateTime.now());
            meterReadings.add(meterReading);
        }
        
        if (CollUtil.isEmpty(meterReadings)) {
            return 0;
        }
        
        // 批量插入
        int result = meterReadingMapper.batchInsert(meterReadings);
        log.info("房东{}批量添加抄表记录成功，月份：{}, 数量：{}", landlordId, readingMonth, result);
        
        return result;
    }
    
    @Override
    public MeterReading getLatestMeterReading(Long roomId, Long landlordId) {
        // 验证房间权限
        validateRoomOwnership(roomId, landlordId);
        
        return meterReadingMapper.selectLatestByRoomId(roomId);
    }
    
    @Override
    public List<MeterReading> getMeterReadingsByMonth(Long landlordId, String readingMonth) {
        return meterReadingMapper.selectByLandlordAndMonth(landlordId, readingMonth);
    }
    
    @Override
    public boolean existsMeterReading(Long roomId, String readingMonth) {
        return meterReadingMapper.countByRoomAndMonth(roomId, readingMonth) > 0;
    }
    
    @Override
    @Transactional
    public Long generateBill(Long readingId, Long landlordId) {
        // 验证抄表记录
        MeterReading meterReading = getMeterReadingById(readingId, landlordId);
        
        // 调用账单服务生成账单
        return billService.generateBillFromMeterReading(readingId, landlordId);
    }
    
    /**
     * 验证房间所有权
     */
    private void validateRoomOwnership(Long roomId, Long landlordId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(ResultCode.ROOM_NOT_FOUND, "房间不存在");
        }
        
        if (propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该房间");
        }
    }
}