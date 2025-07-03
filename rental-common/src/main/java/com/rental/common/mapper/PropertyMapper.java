package com.rental.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rental.common.entity.Property;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房产Mapper接口
 */
@Mapper
public interface PropertyMapper extends BaseMapper<Property> {
    
    /**
     * 根据房东ID查询房产列表
     * @param landlordId 房东ID
     * @return 房产列表
     */
    @Select("SELECT * FROM properties WHERE landlord_id = #{landlordId} ORDER BY created_at DESC")
    List<Property> selectByLandlordId(Long landlordId);
    
    /**
     * 验证房产是否属于指定房东
     * @param propertyId 房产ID
     * @param landlordId 房东ID
     * @return 记录数
     */
    @Select("SELECT COUNT(1) FROM properties WHERE id = #{propertyId} AND landlord_id = #{landlordId}")
    int countByIdAndLandlordId(Long propertyId, Long landlordId);
}