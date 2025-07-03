package com.rental.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.MeterReadingDTO;
import com.rental.common.entity.MeterReading;

import java.util.List;

/**
 * 抄表记录服务接口
 */
public interface MeterReadingService {
    
    /**
     * 分页查询抄表记录
     * @param current 当前页
     * @param size 每页大小
     * @param landlordId 房东ID
     * @param readingMonth 抄表月份
     * @param roomId 房间ID
     * @return 抄表记录分页数据
     */
    IPage<MeterReading> getMeterReadingPage(Long current, Long size, Long landlordId, 
                                           String readingMonth, Long roomId);
    
    /**
     * 根据ID查询抄表记录
     * @param readingId 抄表记录ID
     * @param landlordId 房东ID（权限验证）
     * @return 抄表记录
     */
    MeterReading getMeterReadingById(Long readingId, Long landlordId);
    
    /**
     * 添加抄表记录
     * @param meterReadingDTO 抄表记录信息
     * @param landlordId 房东ID（权限验证）
     * @return 抄表记录ID
     */
    Long addMeterReading(MeterReadingDTO meterReadingDTO, Long landlordId);
    
    /**
     * 更新抄表记录
     * @param meterReadingDTO 抄表记录信息
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean updateMeterReading(MeterReadingDTO meterReadingDTO, Long landlordId);
    
    /**
     * 删除抄表记录
     * @param readingId 抄表记录ID
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean deleteMeterReading(Long readingId, Long landlordId);
    
    /**
     * 批量添加抄表记录
     * @param readingMonth 抄表月份
     * @param readings 抄表记录列表
     * @param landlordId 房东ID（权限验证）
     * @return 成功添加的数量
     */
    int batchAddMeterReadings(String readingMonth, List<MeterReadingDTO> readings, Long landlordId);
    
    /**
     * 获取房间最新抄表记录
     * @param roomId 房间ID
     * @param landlordId 房东ID（权限验证）
     * @return 最新抄表记录
     */
    MeterReading getLatestMeterReading(Long roomId, Long landlordId);
    
    /**
     * 获取指定月份的抄表统计
     * @param landlordId 房东ID
     * @param readingMonth 抄表月份
     * @return 抄表记录列表
     */
    List<MeterReading> getMeterReadingsByMonth(Long landlordId, String readingMonth);
    
    /**
     * 检查抄表记录是否存在
     * @param roomId 房间ID
     * @param readingMonth 抄表月份
     * @return 是否存在
     */
    boolean existsMeterReading(Long roomId, String readingMonth);
    
    /**
     * 自动生成账单（基于抄表记录）
     * @param readingId 抄表记录ID
     * @param landlordId 房东ID（权限验证）
     * @return 账单ID
     */
    Long generateBill(Long readingId, Long landlordId);
}