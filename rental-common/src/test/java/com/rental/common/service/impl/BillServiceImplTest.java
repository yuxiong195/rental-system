package com.rental.common.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rental.common.dto.BillDTO;
import com.rental.common.entity.Bill;
import com.rental.common.entity.MeterReading;
import com.rental.common.entity.Room;
import com.rental.common.enums.ResultCode;
import com.rental.common.exception.BusinessException;
import com.rental.common.mapper.BillMapper;
import com.rental.common.mapper.BillMapper.BillStatistics;
import com.rental.common.mapper.MeterReadingMapper;
import com.rental.common.mapper.PropertyMapper;
import com.rental.common.mapper.RoomMapper;
import com.rental.common.vo.BillVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillMapper billMapper;
    
    @Mock
    private RoomMapper roomMapper;
    
    @Mock
    private PropertyMapper propertyMapper;
    
    @Mock
    private MeterReadingMapper meterReadingMapper;
    
    @InjectMocks
    private BillServiceImpl billService;
    
    private Bill testBill;
    private Room testRoom;
    private MeterReading testMeterReading;
    private BillDTO testBillDTO;
    private BillVO testBillVO;
    
    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testBill = new Bill();
        testBill.setId(1L);
        testBill.setBillNo("B2024070812345");
        testBill.setRoomId(1L);
        testBill.setTenantId(1L);
        testBill.setBillMonth("2024-07");
        testBill.setRentAmount(new BigDecimal("1500.00"));
        testBill.setWaterAmount(new BigDecimal("50.00"));
        testBill.setElectricityAmount(new BigDecimal("120.00"));
        testBill.setTotalAmount(new BigDecimal("1670.00"));
        testBill.setStatus(1);
        testBill.setPaidAmount(BigDecimal.ZERO);
        testBill.setCreatedAt(LocalDateTime.now());
        
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomName("A101");
        testRoom.setPropertyId(1L);
        testRoom.setTenantId(1L);
        testRoom.setMonthlyRent(new BigDecimal("1500.00"));
        testRoom.setWaterPrice(new BigDecimal("2.50"));
        testRoom.setElectricityPrice(new BigDecimal("1.20"));
        testRoom.setCleaningFee(new BigDecimal("50.00"));
        
        testMeterReading = new MeterReading();
        testMeterReading.setId(1L);
        testMeterReading.setRoomId(1L);
        testMeterReading.setReadingMonth("2024-07");
        testMeterReading.setWaterReading(new BigDecimal("120.00"));
        testMeterReading.setElectricityReading(new BigDecimal("580.00"));
        testMeterReading.setPrevWaterReading(new BigDecimal("100.00"));
        testMeterReading.setPrevElectricityReading(new BigDecimal("480.00"));
        testMeterReading.setWaterUsage(new BigDecimal("20.00"));
        testMeterReading.setElectricityUsage(new BigDecimal("100.00"));
        
        testBillDTO = new BillDTO();
        testBillDTO.setId(1L);
        testBillDTO.setRoomId(1L);
        testBillDTO.setBillMonth("2024-07");
        testBillDTO.setRentAmount(new BigDecimal("1500.00"));
        testBillDTO.setMeterReadingId(1L);
        
        testBillVO = new BillVO();
        testBillVO.setId(1L);
        testBillVO.setBillNo("B2024070812345");
        testBillVO.setRoomName("A101");
        testBillVO.setTotalAmount(new BigDecimal("1670.00"));
        testBillVO.setStatus(1);
        testBillVO.setStatusText("待支付");
    }
    
    @Test
    void testGetBillPage() {
        // Arrange
        Page<BillVO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testBillVO));
        mockPage.setTotal(1);
        
        when(billMapper.selectBillPage(any(Page.class), eq(1L), eq("2024-07"), eq(1), eq("A101")))
                .thenReturn(mockPage);
        
        // Act
        IPage<BillVO> result = billService.getBillPage(1L, 10L, 1L, "2024-07", 1, "A101");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("待支付", result.getRecords().get(0).getStatusText());
        
        verify(billMapper).selectBillPage(any(Page.class), eq(1L), eq("2024-07"), eq(1), eq("A101"));
    }
    
    @Test
    void testGetBillById_Success() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        
        // Act
        BillVO result = billService.getBillById(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("B2024070812345", result.getBillNo());
        assertEquals("A101", result.getRoomName());
        assertEquals("待支付", result.getStatusText());
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testGetBillById_NotFound() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(null);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.getBillById(1L, 1L));
        assertEquals(ResultCode.NOT_FOUND, exception.getCode());
        assertEquals("账单不存在", exception.getMessage());
        
        verify(billMapper).selectById(1L);
    }
    
    @Test
    void testGetBillById_NoPermission() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(0);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.getBillById(1L, 1L));
        assertEquals(ResultCode.FORBIDDEN, exception.getCode());
        assertEquals("无权访问该账单", exception.getMessage());
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testGenerateBillFromMeterReading_Success() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(billMapper.insert(any(Bill.class))).thenReturn(1);
        
        // Act
        Long result = billService.generateBillFromMeterReading(1L, 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
        verify(billMapper).insert(any(Bill.class));
    }
    
    @Test
    void testGenerateBillFromMeterReading_MeterReadingNotFound() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(null);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.generateBillFromMeterReading(1L, 1L));
        assertEquals(ResultCode.NOT_FOUND, exception.getCode());
        assertEquals("抄表记录不存在", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
    }
    
    @Test
    void testGenerateBillFromMeterReading_BillAlreadyExists() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.generateBillFromMeterReading(1L, 1L));
        assertEquals(ResultCode.DUPLICATE_DATA, exception.getCode());
        assertEquals("该房间在2024-07的账单已存在", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
    }
    
    @Test
    void testGenerateBillFromMeterReading_RoomNotBoundToTenant() {
        // Arrange
        testRoom.setTenantId(null);
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.generateBillFromMeterReading(1L, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("房间未绑定租客，无法生成账单", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
    }
    
    @Test
    void testCreateBill_Success() {
        // Arrange
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(billMapper.insert(any(Bill.class))).thenReturn(1);
        
        // Act
        Long result = billService.createBill(testBillDTO, 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
        verify(billMapper).insert(any(Bill.class));
    }
    
    @Test
    void testUpdateBill_Success() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.updateById(any(Bill.class))).thenReturn(1);
        
        // Act
        boolean result = billService.updateBill(testBillDTO, 1L);
        
        // Assert
        assertTrue(result);
        
        verify(billMapper, times(2)).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).updateById(any(Bill.class));
    }
    
    @Test
    void testUpdateBill_WrongStatus() {
        // Arrange
        testBill.setStatus(2); // 已支付状态
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.updateBill(testBillDTO, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("只能修改待支付状态的账单", exception.getMessage());
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testDeleteBill_Success() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.deleteById(1L)).thenReturn(1);
        
        // Act
        boolean result = billService.deleteBill(1L, 1L);
        
        // Assert
        assertTrue(result);
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).deleteById(1L);
    }
    
    @Test
    void testDeleteBill_WrongStatus() {
        // Arrange
        testBill.setStatus(2); // 已支付状态
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.deleteBill(1L, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("只能删除待支付状态的账单", exception.getMessage());
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testBatchGenerateBills_Success() {
        // Arrange
        List<MeterReading> meterReadings = Arrays.asList(testMeterReading);
        when(meterReadingMapper.selectByLandlordAndMonth(1L, "2024-07")).thenReturn(meterReadings);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.insert(any(Bill.class))).thenReturn(1);
        
        // Act
        int result = billService.batchGenerateBills("2024-07", null, false, 1L);
        
        // Assert
        assertEquals(1, result);
        
        verify(meterReadingMapper).selectByLandlordAndMonth(1L, "2024-07");
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).insert(any(Bill.class));
    }
    
    @Test
    void testBatchGenerateBills_WithRoomIds() {
        // Arrange
        List<Long> roomIds = Arrays.asList(1L);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(testMeterReading);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(billMapper.insert(any(Bill.class))).thenReturn(1);
        
        // Act
        int result = billService.batchGenerateBills("2024-07", roomIds, false, 1L);
        
        // Assert
        assertEquals(1, result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).selectLatestByRoomId(1L);
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
        verify(billMapper).insert(any(Bill.class));
    }
    
    @Test
    void testBatchGenerateBills_SkipExisting() {
        // Arrange
        List<MeterReading> meterReadings = Arrays.asList(testMeterReading);
        when(meterReadingMapper.selectByLandlordAndMonth(1L, "2024-07")).thenReturn(meterReadings);
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1); // 已存在账单
        
        // Act
        int result = billService.batchGenerateBills("2024-07", null, false, 1L);
        
        // Assert
        assertEquals(0, result);
        
        verify(meterReadingMapper).selectByLandlordAndMonth(1L, "2024-07");
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
        verify(billMapper, never()).insert(any(Bill.class));
    }
    
    @Test
    void testMarkBillAsPaid_Success() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.updateById(any(Bill.class))).thenReturn(1);
        
        // Act
        boolean result = billService.markBillAsPaid(1L, new BigDecimal("1670.00"), "wechat", 1L);
        
        // Assert
        assertTrue(result);
        
        verify(billMapper, times(2)).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).updateById(any(Bill.class));
    }
    
    @Test
    void testMarkBillAsPaid_WrongStatus() {
        // Arrange
        testBill.setStatus(2); // 已支付状态
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> billService.markBillAsPaid(1L, new BigDecimal("1670.00"), "wechat", 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("账单状态不正确", exception.getMessage());
        
        verify(billMapper, times(2)).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testVoidBill_Success() {
        // Arrange
        when(billMapper.selectById(1L)).thenReturn(testBill);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billMapper.updateById(any(Bill.class))).thenReturn(1);
        
        // Act
        boolean result = billService.voidBill(1L, 1L);
        
        // Assert
        assertTrue(result);
        
        verify(billMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billMapper).updateById(any(Bill.class));
    }
    
    @Test
    void testGetBillStatistics() {
        // Arrange
        BillStatistics mockStatistics = new BillStatistics();
        mockStatistics.setTotalCount(10L);
        mockStatistics.setPendingCount(5L);
        mockStatistics.setPaidCount(5L);
        mockStatistics.setTotalAmount(new BigDecimal("16700.00"));
        mockStatistics.setPaidAmount(new BigDecimal("8350.00"));
        
        when(billMapper.selectBillStatistics(1L, "2024-07")).thenReturn(mockStatistics);
        
        // Act
        BillStatistics result = billService.getBillStatistics(1L, "2024-07");
        
        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getTotalCount());
        assertEquals(5L, result.getPendingCount());
        assertEquals(5L, result.getPaidCount());
        assertEquals(new BigDecimal("16700.00"), result.getTotalAmount());
        assertEquals(new BigDecimal("8350.00"), result.getPaidAmount());
        
        verify(billMapper).selectBillStatistics(1L, "2024-07");
    }
    
    @Test
    void testExistsBill() {
        // Arrange
        when(billMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1);
        
        // Act
        boolean result = billService.existsBill(1L, "2024-07");
        
        // Assert
        assertTrue(result);
        
        verify(billMapper).countByRoomAndMonth(1L, "2024-07");
    }
    
    @Test
    void testGetBillsByMonth() {
        // Arrange
        List<BillVO> mockBills = Arrays.asList(testBillVO);
        when(billMapper.selectByLandlordAndMonth(1L, "2024-07")).thenReturn(mockBills);
        
        // Act
        List<BillVO> result = billService.getBillsByMonth(1L, "2024-07");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("待支付", result.get(0).getStatusText());
        
        verify(billMapper).selectByLandlordAndMonth(1L, "2024-07");
    }
}