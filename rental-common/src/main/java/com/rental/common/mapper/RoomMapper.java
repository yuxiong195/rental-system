package com.rental.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.entity.Room;
import com.rental.common.vo.RoomVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房间Mapper接口
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    
    /**
     * 分页查询房间列表（包含房产和租客信息）
     * @param page 分页对象
     * @param landlordId 房东ID
     * @param status 房间状态
     * @param keyword 搜索关键词
     * @return 房间VO列表
     */
    @Select("<script>" +
            "SELECT r.*, p.name as property_name, u.name as tenant_name " +
            "FROM rooms r " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "LEFT JOIN users u ON r.tenant_id = u.id " +
            "WHERE p.landlord_id = #{landlordId} " +
            "<if test='status != null'> AND r.status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (r.room_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR r.tenant_phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY r.created_at DESC" +
            "</script>")
    IPage<RoomVO> selectRoomPage(Page<RoomVO> page, @Param("landlordId") Long landlordId, 
                                 @Param("status") Integer status, @Param("keyword") String keyword);
    
    /**
     * 根据房东ID查询所有房间
     * @param landlordId 房东ID
     * @return 房间列表
     */
    @Select("SELECT r.* FROM rooms r " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "WHERE p.landlord_id = #{landlordId}")
    List<Room> selectByLandlordId(Long landlordId);
    
    /**
     * 绑定租客
     * @param roomId 房间ID
     * @param tenantId 租客ID
     * @param tenantPhone 租客手机号
     * @param rentStartDate 起租日期
     * @return 更新行数
     */
    @Update("UPDATE rooms SET tenant_id = #{tenantId}, tenant_phone = #{tenantPhone}, " +
            "rent_start_date = #{rentStartDate}, status = 2, updated_at = NOW() " +
            "WHERE id = #{roomId}")
    int bindTenant(@Param("roomId") Long roomId, @Param("tenantId") Long tenantId, 
                   @Param("tenantPhone") String tenantPhone, @Param("rentStartDate") String rentStartDate);
    
    /**
     * 解绑租客
     * @param roomId 房间ID
     * @return 更新行数
     */
    @Update("UPDATE rooms SET tenant_id = NULL, tenant_phone = NULL, " +
            "rent_start_date = NULL, status = 1, updated_at = NOW() " +
            "WHERE id = #{roomId}")
    int unbindTenant(Long roomId);
    
    /**
     * 更新房间水电表读数
     * @param roomId 房间ID
     * @param waterReading 水表读数
     * @param electricityReading 电表读数
     * @return 更新行数
     */
    @Update("UPDATE rooms SET last_water_reading = #{waterReading}, " +
            "last_electricity_reading = #{electricityReading}, updated_at = NOW() " +
            "WHERE id = #{roomId}")
    int updateMeterReadings(@Param("roomId") Long roomId, 
                           @Param("waterReading") String waterReading,
                           @Param("electricityReading") String electricityReading);
}