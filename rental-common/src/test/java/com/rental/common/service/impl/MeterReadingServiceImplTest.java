package com.rental.common.service.impl;

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
import com.rental.common.service.BillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceImplTest {

    @Mock
    private MeterReadingMapper meterReadingMapper;
    
    @Mock
    private RoomMapper roomMapper;
    
    @Mock
    private PropertyMapper propertyMapper;
    
    @Mock
    private BillService billService;
    
    @InjectMocks
    private MeterReadingServiceImpl meterReadingService;
    
    private MeterReading testMeterReading;
    private Room testRoom;
    private MeterReadingDTO testMeterReadingDTO;
    
    @BeforeEach
    void setUp() {
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
        testMeterReading.setReadingDate(LocalDate.now());
        testMeterReading.setCreatedAt(LocalDateTime.now());
        
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setRoomName("A101");
        testRoom.setPropertyId(1L);
        testRoom.setTenantId(1L);
        testRoom.setLastWaterReading(new BigDecimal("100.00"));
        testRoom.setLastElectricityReading(new BigDecimal("480.00"));
        
        testMeterReadingDTO = new MeterReadingDTO();
        testMeterReadingDTO.setId(1L);
        testMeterReadingDTO.setRoomId(1L);
        testMeterReadingDTO.setReadingMonth("2024-07");
        testMeterReadingDTO.setWaterReading(new BigDecimal("120.00"));
        testMeterReadingDTO.setElectricityReading(new BigDecimal("580.00"));
        testMeterReadingDTO.setReadingDate(LocalDate.now());
    }
    
    @Test
    void testGetMeterReadingPage() {
        // Arrange
        Page<MeterReading> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testMeterReading));
        mockPage.setTotal(1);
        
        when(meterReadingMapper.selectMeterReadingPage(any(Page.class), eq(1L), eq("2024-07"), eq(1L)))
                .thenReturn(mockPage);
        
        // Act
        IPage<MeterReading> result = meterReadingService.getMeterReadingPage(1L, 10L, 1L, "2024-07", 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        
        verify(meterReadingMapper).selectMeterReadingPage(any(Page.class), eq(1L), eq("2024-07"), eq(1L));
    }
    
    @Test
    void testGetMeterReadingById_Success() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        
        // Act
        MeterReading result = meterReadingService.getMeterReadingById(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("2024-07", result.getReadingMonth());
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testGetMeterReadingById_NotFound() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(null);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.getMeterReadingById(1L, 1L));
        assertEquals(ResultCode.NOT_FOUND, exception.getCode());
        assertEquals("抄表记录不存在", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
    }
    
    @Test
    void testGetMeterReadingById_NoPermission() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(0);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.getMeterReadingById(1L, 1L));
        assertEquals(ResultCode.FORBIDDEN, exception.getCode());
        assertEquals("无权访问该房间", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
    }
    
    @Test
    void testAddMeterReading_Success() {
        // Arrange
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(null);
        when(meterReadingMapper.insert(any(MeterReading.class))).thenReturn(1);
        
        // Act
        Long result = meterReadingService.addMeterReading(testMeterReadingDTO, 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).selectLatestByRoomId(1L);
        verify(meterReadingMapper).insert(any(MeterReading.class));
    }
    
    @Test
    void testAddMeterReading_WithPreviousReading() {
        // Arrange
        MeterReading previousReading = new MeterReading();
        previousReading.setWaterReading(new BigDecimal("100.00"));
        previousReading.setElectricityReading(new BigDecimal("480.00"));
        
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(previousReading);
        when(meterReadingMapper.insert(any(MeterReading.class))).thenReturn(1);
        
        // Act
        Long result = meterReadingService.addMeterReading(testMeterReadingDTO, 1L);
        
        // Assert
        assertNotNull(result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).selectLatestByRoomId(1L);
        verify(meterReadingMapper).insert(any(MeterReading.class));
    }
    
    @Test
    void testAddMeterReading_DuplicateRecord() {
        // Arrange
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.addMeterReading(testMeterReadingDTO, 1L));
        assertEquals(ResultCode.DUPLICATE_DATA, exception.getCode());
        assertEquals("该房间在2024-07的抄表记录已存在", exception.getMessage());
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
    }
    
    @Test
    void testAddMeterReading_InvalidWaterReading() {
        // Arrange
        testMeterReadingDTO.setWaterReading(new BigDecimal("50.00")); // 小于上期读数
        
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(null);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.addMeterReading(testMeterReadingDTO, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("水表读数不能小于上期读数", exception.getMessage());
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).selectLatestByRoomId(1L);
    }
    
    @Test
    void testAddMeterReading_InvalidElectricityReading() {
        // Arrange
        testMeterReadingDTO.setElectricityReading(new BigDecimal("400.00")); // 小于上期读数
        
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(null);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.addMeterReading(testMeterReadingDTO, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("电表读数不能小于上期读数", exception.getMessage());
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).selectLatestByRoomId(1L);
    }
    
    @Test
    void testUpdateMeterReading_Success() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1);
        when(meterReadingMapper.updateById(any(MeterReading.class))).thenReturn(1);
        
        // Act
        boolean result = meterReadingService.updateMeterReading(testMeterReadingDTO, 1L);
        
        // Assert
        assertTrue(result);
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).updateById(any(MeterReading.class));
    }
    
    @Test
    void testUpdateMeterReading_DuplicateMonth() {
        // Arrange
        testMeterReadingDTO.setReadingMonth("2024-08"); // 修改为不同月份
        
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-08")).thenReturn(1);
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.updateMeterReading(testMeterReadingDTO, 1L));
        assertEquals(ResultCode.DUPLICATE_DATA, exception.getCode());
        assertEquals("该房间在2024-08的抄表记录已存在", exception.getMessage());
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-08");
    }
    
    @Test
    void testDeleteMeterReading_Success() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.deleteById(1L)).thenReturn(1);
        
        // Act
        boolean result = meterReadingService.deleteMeterReading(1L, 1L);
        
        // Assert
        assertTrue(result);
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).deleteById(1L);
    }
    
    @Test
    void testBatchAddMeterReadings_Success() {
        // Arrange
        List<MeterReadingDTO> readings = Arrays.asList(testMeterReadingDTO);
        
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(0);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(null);
        when(meterReadingMapper.batchInsert(anyList())).thenReturn(1);
        
        // Act
        int result = meterReadingService.batchAddMeterReadings("2024-07", readings, 1L);
        
        // Assert
        assertEquals(1, result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper).selectLatestByRoomId(1L);
        verify(meterReadingMapper).batchInsert(anyList());
    }
    
    @Test
    void testBatchAddMeterReadings_EmptyList() {
        // Arrange
        List<MeterReadingDTO> readings = Arrays.asList();
        
        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> meterReadingService.batchAddMeterReadings("2024-07", readings, 1L));
        assertEquals(ResultCode.PARAM_ERROR, exception.getCode());
        assertEquals("抄表记录列表不能为空", exception.getMessage());
    }
    
    @Test
    void testBatchAddMeterReadings_SkipExisting() {
        // Arrange
        List<MeterReadingDTO> readings = Arrays.asList(testMeterReadingDTO);
        
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1); // 已存在
        
        // Act
        int result = meterReadingService.batchAddMeterReadings("2024-07", readings, 1L);
        
        // Assert
        assertEquals(0, result);
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
        verify(meterReadingMapper, never()).batchInsert(anyList());
    }
    
    @Test
    void testGetLatestMeterReading_Success() {
        // Arrange
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(meterReadingMapper.selectLatestByRoomId(1L)).thenReturn(testMeterReading);
        
        // Act
        MeterReading result = meterReadingService.getLatestMeterReading(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(meterReadingMapper).selectLatestByRoomId(1L);
    }
    
    @Test
    void testGetMeterReadingsByMonth() {
        // Arrange
        List<MeterReading> mockReadings = Arrays.asList(testMeterReading);
        when(meterReadingMapper.selectByLandlordAndMonth(1L, "2024-07")).thenReturn(mockReadings);
        
        // Act
        List<MeterReading> result = meterReadingService.getMeterReadingsByMonth(1L, "2024-07");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        verify(meterReadingMapper).selectByLandlordAndMonth(1L, "2024-07");
    }
    
    @Test
    void testExistsMeterReading() {
        // Arrange
        when(meterReadingMapper.countByRoomAndMonth(1L, "2024-07")).thenReturn(1);
        
        // Act
        boolean result = meterReadingService.existsMeterReading(1L, "2024-07");
        
        // Assert
        assertTrue(result);
        
        verify(meterReadingMapper).countByRoomAndMonth(1L, "2024-07");
    }
    
    @Test
    void testGenerateBill() {
        // Arrange
        when(meterReadingMapper.selectById(1L)).thenReturn(testMeterReading);
        when(roomMapper.selectById(1L)).thenReturn(testRoom);
        when(propertyMapper.countByIdAndLandlordId(1L, 1L)).thenReturn(1);
        when(billService.generateBillFromMeterReading(1L, 1L)).thenReturn(1L);
        
        // Act
        Long result = meterReadingService.generateBill(1L, 1L);
        
        // Assert
        assertEquals(1L, result);
        
        verify(meterReadingMapper).selectById(1L);
        verify(roomMapper).selectById(1L);
        verify(propertyMapper).countByIdAndLandlordId(1L, 1L);
        verify(billService).generateBillFromMeterReading(1L, 1L);
    }
}