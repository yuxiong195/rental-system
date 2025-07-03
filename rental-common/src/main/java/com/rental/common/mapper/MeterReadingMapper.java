package com.rental.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.entity.MeterReading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 抄表记录Mapper接口
 */
@Mapper
public interface MeterReadingMapper extends BaseMapper<MeterReading> {
    
    /**
     * 分页查询抄表记录（包含房间信息）
     * @param page 分页对象
     * @param landlordId 房东ID
     * @param readingMonth 抄表月份
     * @param roomId 房间ID
     * @return 抄表记录列表
     */
    @Select("<script>" +
            "SELECT mr.*, r.room_name, p.name as property_name " +
            "FROM meter_readings mr " +
            "LEFT JOIN rooms r ON mr.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "WHERE p.landlord_id = #{landlordId} " +
            "<if test='readingMonth != null and readingMonth != \"\"'> " +
            "AND mr.reading_month = #{readingMonth} " +
            "</if> " +
            "<if test='roomId != null'> AND mr.room_id = #{roomId} </if> " +
            "ORDER BY mr.reading_month DESC, mr.created_at DESC" +
            "</script>")
    IPage<MeterReading> selectMeterReadingPage(Page<MeterReading> page, 
                                              @Param("landlordId") Long landlordId,
                                              @Param("readingMonth") String readingMonth,
                                              @Param("roomId") Long roomId);
    
    /**
     * 查询房间最新的抄表记录
     * @param roomId 房间ID
     * @return 最新抄表记录
     */
    @Select("SELECT * FROM meter_readings WHERE room_id = #{roomId} " +
            "ORDER BY reading_month DESC, created_at DESC LIMIT 1")
    MeterReading selectLatestByRoomId(Long roomId);
    
    /**
     * 查询指定月份房东的所有抄表记录
     * @param landlordId 房东ID
     * @param readingMonth 抄表月份
     * @return 抄表记录列表
     */
    @Select("SELECT mr.*, r.room_name, p.name as property_name " +
            "FROM meter_readings mr " +
            "LEFT JOIN rooms r ON mr.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "WHERE p.landlord_id = #{landlordId} AND mr.reading_month = #{readingMonth} " +
            "ORDER BY r.room_name")
    List<MeterReading> selectByLandlordAndMonth(@Param("landlordId") Long landlordId, 
                                               @Param("readingMonth") String readingMonth);
    
    /**
     * 检查抄表记录是否存在
     * @param roomId 房间ID
     * @param readingMonth 抄表月份
     * @return 记录数
     */
    @Select("SELECT COUNT(1) FROM meter_readings " +
            "WHERE room_id = #{roomId} AND reading_month = #{readingMonth}")
    int countByRoomAndMonth(@Param("roomId") Long roomId, @Param("readingMonth") String readingMonth);
    
    /**
     * 批量插入抄表记录
     * @param meterReadings 抄表记录列表
     * @return 插入行数
     */
    int batchInsert(@Param("list") List<MeterReading> meterReadings);
}