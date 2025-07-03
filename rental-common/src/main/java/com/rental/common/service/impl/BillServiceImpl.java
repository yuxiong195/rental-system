package com.rental.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.dto.BillDTO;
import com.rental.common.entity.Bill;
import com.rental.common.entity.MeterReading;
import com.rental.common.entity.Room;
import com.rental.common.enums.ResultCode;
import com.rental.common.exception.BusinessException;
import com.rental.common.mapper.BillMapper;
import com.rental.common.mapper.MeterReadingMapper;
import com.rental.common.mapper.PropertyMapper;
import com.rental.common.mapper.RoomMapper;
import com.rental.common.service.BillService;
import com.rental.common.vo.BillVO;
import com.rental.common.mapper.BillMapper.BillStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 账单管理服务实现类
 */
@Slf4j
@Service
public class BillServiceImpl implements BillService {
    
    @Autowired
    private BillMapper billMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private PropertyMapper propertyMapper;
    
    @Autowired
    private MeterReadingMapper meterReadingMapper;
    
    @Override
    public IPage<BillVO> getBillPage(Long current, Long size, Long landlordId, 
                                     String billMonth, Integer status, String keyword) {
        Page<BillVO> page = new Page<>(current, size);
        IPage<BillVO> billPage = billMapper.selectBillPage(page, landlordId, billMonth, status, keyword);
        
        // 设置状态文本
        billPage.getRecords().forEach(bill -> {
            bill.setStatusText(getBillStatusText(bill.getStatus()));
        });
        
        return billPage;
    }
    
    @Override
    public BillVO getBillById(Long billId, Long landlordId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账单不存在");
        }
        
        // 验证权限：检查账单是否属于该房东
        validateBillOwnership(billId, landlordId);
        
        // 转换为VO并获取详细信息
        BillVO billVO = new BillVO();
        BeanUtils.copyProperties(bill, billVO);
        
        // 获取关联信息
        Room room = roomMapper.selectById(bill.getRoomId());
        if (room != null) {
            billVO.setRoomName(room.getRoomName());
            // 获取房产名称等其他信息...
        }
        
        billVO.setStatusText(getBillStatusText(bill.getStatus()));
        
        return billVO;
    }
    
    @Override
    @Transactional
    public Long generateBillFromMeterReading(Long meterReadingId, Long landlordId) {
        // 获取抄表记录
        MeterReading meterReading = meterReadingMapper.selectById(meterReadingId);
        if (meterReading == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "抄表记录不存在");
        }
        
        // 验证权限
        validateRoomOwnership(meterReading.getRoomId(), landlordId);
        
        // 检查是否已生成账单
        if (billMapper.countByRoomAndMonth(meterReading.getRoomId(), meterReading.getReadingMonth()) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, 
                    "该房间在" + meterReading.getReadingMonth() + "的账单已存在");
        }
        
        // 获取房间信息
        Room room = roomMapper.selectById(meterReading.getRoomId());
        if (room == null || room.getTenantId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "房间未绑定租客，无法生成账单");
        }
        
        // 计算各项费用
        BillCalculation calculation = calculateBillAmounts(room, meterReading);
        
        // 创建账单
        Bill bill = new Bill();
        bill.setBillNo(generateBillNo());
        bill.setRoomId(room.getId());
        bill.setTenantId(room.getTenantId());
        bill.setBillMonth(meterReading.getReadingMonth());
        bill.setMeterReadingId(meterReadingId);
        
        // 设置费用金额
        bill.setRentAmount(calculation.getRentAmount());
        bill.setWaterAmount(calculation.getWaterAmount());
        bill.setElectricityAmount(calculation.getElectricityAmount());
        bill.setCleaningAmount(calculation.getCleaningAmount());
        bill.setOtherDetails(calculation.getOtherDetails());
        bill.setTotalAmount(calculation.getTotalAmount());
        
        bill.setStatus(1); // 待支付
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        billMapper.insert(bill);
        
        log.info("房东{}基于抄表记录{}生成账单成功，账单ID：{}, 总金额：{}", 
                landlordId, meterReadingId, bill.getId(), bill.getTotalAmount());
        
        return bill.getId();
    }
    
    @Override
    @Transactional
    public Long createBill(BillDTO billDTO, Long landlordId) {
        // 验证房间权限
        validateRoomOwnership(billDTO.getRoomId(), landlordId);
        
        // 检查账单是否已存在
        if (billMapper.countByRoomAndMonth(billDTO.getRoomId(), billDTO.getBillMonth()) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_DATA, 
                    "该房间在" + billDTO.getBillMonth() + "的账单已存在");
        }
        
        // 获取房间信息
        Room room = roomMapper.selectById(billDTO.getRoomId());
        if (room == null || room.getTenantId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "房间未绑定租客，无法创建账单");
        }
        
        // 创建账单
        Bill bill = new Bill();
        bill.setBillNo(generateBillNo());
        bill.setRoomId(billDTO.getRoomId());
        bill.setTenantId(room.getTenantId());
        bill.setBillMonth(billDTO.getBillMonth());
        bill.setMeterReadingId(billDTO.getMeterReadingId());
        
        // 如果指定了租金金额，使用指定值，否则使用房间配置
        bill.setRentAmount(billDTO.getRentAmount() != null ? 
                billDTO.getRentAmount() : room.getMonthlyRent());
        
        // 其他费用处理
        bill.setOtherDetails(billDTO.getOtherDetails());
        
        // 计算总金额（如果没有抄表记录，只计算固定费用）
        BigDecimal totalAmount = bill.getRentAmount();
        if (room.getCleaningFee() != null) {
            bill.setCleaningAmount(room.getCleaningFee());
            totalAmount = totalAmount.add(room.getCleaningFee());
        }
        
        // 处理其他费用
        if (StrUtil.isNotBlank(bill.getOtherDetails())) {
            // 解析JSON并累计金额
            // 这里简化处理，实际应该解析JSON
            totalAmount = totalAmount.add(BigDecimal.ZERO);
        }
        
        bill.setTotalAmount(totalAmount);
        bill.setStatus(1); // 待支付
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        
        billMapper.insert(bill);
        
        log.info("房东{}手动创建账单成功，账单ID：{}", landlordId, bill.getId());
        
        return bill.getId();
    }
    
    @Override
    @Transactional
    public boolean updateBill(BillDTO billDTO, Long landlordId) {
        // 验证账单权限
        validateBillOwnership(billDTO.getId(), landlordId);
        
        Bill bill = billMapper.selectById(billDTO.getId());
        if (bill.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能修改待支付状态的账单");
        }
        
        // 更新账单信息
        Bill updateBill = new Bill();
        updateBill.setId(billDTO.getId());
        if (billDTO.getRentAmount() != null) {
            updateBill.setRentAmount(billDTO.getRentAmount());
        }
        updateBill.setOtherDetails(billDTO.getOtherDetails());
        updateBill.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算总金额
        Bill currentBill = billMapper.selectById(billDTO.getId());
        BigDecimal totalAmount = calculateUpdatedTotalAmount(currentBill, updateBill);
        updateBill.setTotalAmount(totalAmount);
        
        int result = billMapper.updateById(updateBill);
        log.info("房东{}更新账单成功，账单ID：{}", landlordId, billDTO.getId());
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteBill(Long billId, Long landlordId) {
        // 验证账单权限
        validateBillOwnership(billId, landlordId);
        
        Bill bill = billMapper.selectById(billId);
        if (bill.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能删除待支付状态的账单");
        }
        
        int result = billMapper.deleteById(billId);
        log.info("房东{}删除账单成功，账单ID：{}", landlordId, billId);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public int batchGenerateBills(String billMonth, List<Long> roomIds, Boolean overwrite, Long landlordId) {
        // 获取需要生成账单的抄表记录
        List<MeterReading> meterReadings;
        if (CollUtil.isNotEmpty(roomIds)) {
            // 指定房间的抄表记录
            meterReadings = new ArrayList<>();
            for (Long roomId : roomIds) {
                validateRoomOwnership(roomId, landlordId);
                MeterReading reading = meterReadingMapper.selectLatestByRoomId(roomId);
                if (reading != null && billMonth.equals(reading.getReadingMonth())) {
                    meterReadings.add(reading);
                }
            }
        } else {
            // 所有已抄表房间
            meterReadings = meterReadingMapper.selectByLandlordAndMonth(landlordId, billMonth);
        }
        
        int generatedCount = 0;
        for (MeterReading meterReading : meterReadings) {
            try {
                // 检查是否已存在账单
                if (!overwrite && billMapper.countByRoomAndMonth(meterReading.getRoomId(), billMonth) > 0) {
                    log.warn("房间{}在{}的账单已存在，跳过", meterReading.getRoomId(), billMonth);
                    continue;
                }
                
                // 如果覆盖模式，先删除已存在的账单
                if (overwrite) {
                    // 这里可以添加删除逻辑
                }
                
                // 生成账单
                generateBillFromMeterReading(meterReading.getId(), landlordId);
                generatedCount++;
                
            } catch (Exception e) {
                log.error("生成账单失败，抄表记录ID：{}, 错误：{}", meterReading.getId(), e.getMessage());
            }
        }
        
        log.info("房东{}批量生成账单完成，月份：{}, 成功数量：{}", landlordId, billMonth, generatedCount);
        
        return generatedCount;
    }
    
    @Override
    @Transactional
    public boolean markBillAsPaid(Long billId, BigDecimal paidAmount, String paymentMethod, Long landlordId) {
        // 验证账单权限
        validateBillOwnership(billId, landlordId);
        
        Bill bill = billMapper.selectById(billId);
        if (bill.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "账单状态不正确");
        }
        
        // 更新账单状态
        Bill updateBill = new Bill();
        updateBill.setId(billId);
        updateBill.setStatus(2); // 已支付
        updateBill.setPaidAmount(paidAmount);
        updateBill.setPaidAt(LocalDateTime.now());
        updateBill.setPaymentMethod(paymentMethod);
        updateBill.setUpdatedAt(LocalDateTime.now());
        
        int result = billMapper.updateById(updateBill);
        log.info("房东{}标记账单{}为已支付，实付金额：{}", landlordId, billId, paidAmount);
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean voidBill(Long billId, Long landlordId) {
        // 验证账单权限
        validateBillOwnership(billId, landlordId);
        
        Bill updateBill = new Bill();
        updateBill.setId(billId);
        updateBill.setStatus(3); // 已作废
        updateBill.setUpdatedAt(LocalDateTime.now());
        
        int result = billMapper.updateById(updateBill);
        log.info("房东{}作废账单成功，账单ID：{}", landlordId, billId);
        
        return result > 0;
    }
    
    @Override
    public BillStatistics getBillStatistics(Long landlordId, String billMonth) {
        return billMapper.selectBillStatistics(landlordId, billMonth);
    }
    
    @Override
    public boolean existsBill(Long roomId, String billMonth) {
        return billMapper.countByRoomAndMonth(roomId, billMonth) > 0;
    }
    
    @Override
    public List<BillVO> getBillsByMonth(Long landlordId, String billMonth) {
        List<BillVO> bills = billMapper.selectByLandlordAndMonth(landlordId, billMonth);
        bills.forEach(bill -> bill.setStatusText(getBillStatusText(bill.getStatus())));
        return bills;
    }
    
    /**
     * 验证账单所有权
     */
    private void validateBillOwnership(Long billId, Long landlordId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账单不存在");
        }
        
        Room room = roomMapper.selectById(bill.getRoomId());
        if (room == null || propertyMapper.countByIdAndLandlordId(room.getPropertyId(), landlordId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该账单");
        }
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
    
    /**
     * 计算账单金额
     */
    private BillCalculation calculateBillAmounts(Room room, MeterReading meterReading) {
        BillCalculation calculation = new BillCalculation();
        
        // 租金
        calculation.setRentAmount(room.getMonthlyRent() != null ? room.getMonthlyRent() : BigDecimal.ZERO);
        
        // 水费
        BigDecimal waterAmount = BigDecimal.ZERO;
        if (room.getWaterPrice() != null && meterReading.getWaterUsage() != null) {
            waterAmount = room.getWaterPrice().multiply(meterReading.getWaterUsage())
                    .setScale(2, RoundingMode.HALF_UP);
        }
        calculation.setWaterAmount(waterAmount);
        
        // 电费
        BigDecimal electricityAmount = BigDecimal.ZERO;
        if (room.getElectricityPrice() != null && meterReading.getElectricityUsage() != null) {
            electricityAmount = room.getElectricityPrice().multiply(meterReading.getElectricityUsage())
                    .setScale(2, RoundingMode.HALF_UP);
        }
        calculation.setElectricityAmount(electricityAmount);
        
        // 卫生费
        calculation.setCleaningAmount(room.getCleaningFee() != null ? room.getCleaningFee() : BigDecimal.ZERO);
        
        // 其他费用（从房间配置中获取）
        calculation.setOtherDetails(room.getOtherFees());
        BigDecimal otherAmount = BigDecimal.ZERO;
        // TODO: 解析JSON并计算其他费用总额
        
        // 计算总金额
        BigDecimal totalAmount = calculation.getRentAmount()
                .add(calculation.getWaterAmount())
                .add(calculation.getElectricityAmount())
                .add(calculation.getCleaningAmount())
                .add(otherAmount);
        
        calculation.setTotalAmount(totalAmount);
        
        return calculation;
    }
    
    /**
     * 重新计算更新后的总金额
     */
    private BigDecimal calculateUpdatedTotalAmount(Bill currentBill, Bill updateBill) {
        BigDecimal rentAmount = updateBill.getRentAmount() != null ? 
                updateBill.getRentAmount() : currentBill.getRentAmount();
        
        BigDecimal totalAmount = rentAmount
                .add(currentBill.getWaterAmount() != null ? currentBill.getWaterAmount() : BigDecimal.ZERO)
                .add(currentBill.getElectricityAmount() != null ? currentBill.getElectricityAmount() : BigDecimal.ZERO)
                .add(currentBill.getCleaningAmount() != null ? currentBill.getCleaningAmount() : BigDecimal.ZERO);
        
        // TODO: 处理其他费用的更新
        
        return totalAmount;
    }
    
    /**
     * 生成账单编号
     */
    private String generateBillNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 这里可以加入更复杂的编号生成逻辑，比如加入序列号
        return "B" + dateStr + System.currentTimeMillis() % 100000;
    }
    
    /**
     * 获取账单状态文本
     */
    private String getBillStatusText(Integer status) {
        switch (status) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "已作废";
            default: return "未知";
        }
    }
    
    /**
     * 账单计算结果内部类
     */
    private static class BillCalculation {
        private BigDecimal rentAmount;
        private BigDecimal waterAmount;
        private BigDecimal electricityAmount;
        private BigDecimal cleaningAmount;
        private String otherDetails;
        private BigDecimal totalAmount;
        
        // getters and setters
        public BigDecimal getRentAmount() { return rentAmount; }
        public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
        public BigDecimal getWaterAmount() { return waterAmount; }
        public void setWaterAmount(BigDecimal waterAmount) { this.waterAmount = waterAmount; }
        public BigDecimal getElectricityAmount() { return electricityAmount; }
        public void setElectricityAmount(BigDecimal electricityAmount) { this.electricityAmount = electricityAmount; }
        public BigDecimal getCleaningAmount() { return cleaningAmount; }
        public void setCleaningAmount(BigDecimal cleaningAmount) { this.cleaningAmount = cleaningAmount; }
        public String getOtherDetails() { return otherDetails; }
        public void setOtherDetails(String otherDetails) { this.otherDetails = otherDetails; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}