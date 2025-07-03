package com.rental.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.entity.Bill;
import com.rental.common.vo.BillVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账单Mapper接口
 */
@Mapper
public interface BillMapper extends BaseMapper<Bill> {
    
    /**
     * 分页查询账单列表（包含房间和租客信息）
     * @param page 分页对象
     * @param landlordId 房东ID
     * @param billMonth 账单月份
     * @param status 账单状态
     * @param keyword 搜索关键词
     * @return 账单VO列表
     */
    @Select("<script>" +
            "SELECT b.*, r.room_name, p.name as property_name, " +
            "u.name as tenant_name, u.phone as tenant_phone, " +
            "mr.water_usage, mr.electricity_usage " +
            "FROM bills b " +
            "LEFT JOIN rooms r ON b.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "LEFT JOIN users u ON b.tenant_id = u.id " +
            "LEFT JOIN meter_readings mr ON b.meter_reading_id = mr.id " +
            "WHERE p.landlord_id = #{landlordId} " +
            "<if test='billMonth != null and billMonth != \"\"'> " +
            "AND b.bill_month = #{billMonth} " +
            "</if> " +
            "<if test='status != null'> AND b.status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (b.bill_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR r.room_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if> " +
            "ORDER BY b.created_at DESC" +
            "</script>")
    IPage<BillVO> selectBillPage(Page<BillVO> page, @Param("landlordId") Long landlordId,
                                 @Param("billMonth") String billMonth, @Param("status") Integer status,
                                 @Param("keyword") String keyword);
    
    /**
     * 根据房东ID查询所有账单
     * @param landlordId 房东ID
     * @return 账单列表
     */
    @Select("SELECT b.* FROM bills b " +
            "LEFT JOIN rooms r ON b.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "WHERE p.landlord_id = #{landlordId}")
    List<Bill> selectByLandlordId(Long landlordId);
    
    /**
     * 检查账单是否存在
     * @param roomId 房间ID
     * @param billMonth 账单月份
     * @return 记录数
     */
    @Select("SELECT COUNT(1) FROM bills " +
            "WHERE room_id = #{roomId} AND bill_month = #{billMonth}")
    int countByRoomAndMonth(@Param("roomId") Long roomId, @Param("billMonth") String billMonth);
    
    /**
     * 查询指定月份房东的所有账单
     * @param landlordId 房东ID
     * @param billMonth 账单月份
     * @return 账单列表
     */
    @Select("SELECT b.*, r.room_name, p.name as property_name, " +
            "u.name as tenant_name, u.phone as tenant_phone " +
            "FROM bills b " +
            "LEFT JOIN rooms r ON b.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "LEFT JOIN users u ON b.tenant_id = u.id " +
            "WHERE p.landlord_id = #{landlordId} AND b.bill_month = #{billMonth} " +
            "ORDER BY r.room_name")
    List<BillVO> selectByLandlordAndMonth(@Param("landlordId") Long landlordId, 
                                          @Param("billMonth") String billMonth);
    
    /**
     * 查询房东的账单统计信息
     * @param landlordId 房东ID
     * @param billMonth 账单月份（可选）
     * @return 统计信息
     */
    @Select("<script>" +
            "SELECT " +
            "COUNT(1) as total_count, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN status = 2 THEN 1 END) as paid_count, " +
            "COALESCE(SUM(total_amount), 0) as total_amount, " +
            "COALESCE(SUM(paid_amount), 0) as paid_amount " +
            "FROM bills b " +
            "LEFT JOIN rooms r ON b.room_id = r.id " +
            "LEFT JOIN properties p ON r.property_id = p.id " +
            "WHERE p.landlord_id = #{landlordId} " +
            "<if test='billMonth != null and billMonth != \"\"'> " +
            "AND b.bill_month = #{billMonth} " +
            "</if>" +
            "</script>")
    BillStatistics selectBillStatistics(@Param("landlordId") Long landlordId,
                                       @Param("billMonth") String billMonth);
    
    /**
     * 账单统计信息内部类
     */
    class BillStatistics {
        private Long totalCount;
        private Long pendingCount;
        private Long paidCount;
        private java.math.BigDecimal totalAmount;
        private java.math.BigDecimal paidAmount;
        
        // getters and setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
        public Long getPaidCount() { return paidCount; }
        public void setPaidCount(Long paidCount) { this.paidCount = paidCount; }
        public java.math.BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public java.math.BigDecimal getPaidAmount() { return paidAmount; }
        public void setPaidAmount(java.math.BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    }
}