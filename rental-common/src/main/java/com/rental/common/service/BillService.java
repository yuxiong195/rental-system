package com.rental.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rental.common.dto.BillDTO;
import com.rental.common.entity.Bill;
import com.rental.common.vo.BillVO;
import com.rental.common.mapper.BillMapper.BillStatistics;

import java.util.List;

/**
 * 账单管理服务接口
 */
public interface BillService {
    
    /**
     * 分页查询账单列表
     * @param current 当前页
     * @param size 每页大小
     * @param landlordId 房东ID
     * @param billMonth 账单月份
     * @param status 账单状态
     * @param keyword 搜索关键词
     * @return 账单分页数据
     */
    IPage<BillVO> getBillPage(Long current, Long size, Long landlordId, 
                              String billMonth, Integer status, String keyword);
    
    /**
     * 根据ID查询账单详情
     * @param billId 账单ID
     * @param landlordId 房东ID（权限验证）
     * @return 账单信息
     */
    BillVO getBillById(Long billId, Long landlordId);
    
    /**
     * 基于抄表记录生成账单
     * @param meterReadingId 抄表记录ID
     * @param landlordId 房东ID（权限验证）
     * @return 账单ID
     */
    Long generateBillFromMeterReading(Long meterReadingId, Long landlordId);
    
    /**
     * 手动创建账单
     * @param billDTO 账单信息
     * @param landlordId 房东ID（权限验证）
     * @return 账单ID
     */
    Long createBill(BillDTO billDTO, Long landlordId);
    
    /**
     * 更新账单信息
     * @param billDTO 账单信息
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean updateBill(BillDTO billDTO, Long landlordId);
    
    /**
     * 删除账单
     * @param billId 账单ID
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean deleteBill(Long billId, Long landlordId);
    
    /**
     * 批量生成账单
     * @param billMonth 账单月份
     * @param roomIds 房间ID列表（可选）
     * @param overwrite 是否覆盖已存在的账单
     * @param landlordId 房东ID（权限验证）
     * @return 生成的账单数量
     */
    int batchGenerateBills(String billMonth, List<Long> roomIds, Boolean overwrite, Long landlordId);
    
    /**
     * 标记账单为已支付
     * @param billId 账单ID
     * @param paidAmount 实付金额
     * @param paymentMethod 支付方式
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean markBillAsPaid(Long billId, java.math.BigDecimal paidAmount, 
                          String paymentMethod, Long landlordId);
    
    /**
     * 作废账单
     * @param billId 账单ID
     * @param landlordId 房东ID（权限验证）
     * @return 是否成功
     */
    boolean voidBill(Long billId, Long landlordId);
    
    /**
     * 获取账单统计信息
     * @param landlordId 房东ID
     * @param billMonth 账单月份（可选）
     * @return 统计信息
     */
    BillStatistics getBillStatistics(Long landlordId, String billMonth);
    
    /**
     * 检查账单是否存在
     * @param roomId 房间ID
     * @param billMonth 账单月份
     * @return 是否存在
     */
    boolean existsBill(Long roomId, String billMonth);
    
    /**
     * 获取指定月份的账单列表
     * @param landlordId 房东ID
     * @param billMonth 账单月份
     * @return 账单列表
     */
    List<BillVO> getBillsByMonth(Long landlordId, String billMonth);
}