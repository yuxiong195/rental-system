package com.rental.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rental.common.dto.BillDTO;
import com.rental.common.entity.Bill;
import com.rental.common.entity.MeterReading;
import com.rental.common.entity.Room;
import com.rental.common.mapper.BillMapper;
import com.rental.common.mapper.MeterReadingMapper;
import com.rental.common.mapper.RoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BillControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private BillMapper billMapper;
    
    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private MeterReadingMapper meterReadingMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    
    private Room testRoom;
    private MeterReading testMeterReading;
    private Bill testBill;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 创建测试数据
        testRoom = new Room();
        testRoom.setRoomName("A101");
        testRoom.setPropertyId(1L);
        testRoom.setTenantId(1L);
        testRoom.setMonthlyRent(new BigDecimal("1500.00"));
        testRoom.setWaterPrice(new BigDecimal("2.50"));
        testRoom.setElectricityPrice(new BigDecimal("1.20"));
        testRoom.setCleaningFee(new BigDecimal("50.00"));
        roomMapper.insert(testRoom);
        
        testMeterReading = new MeterReading();
        testMeterReading.setRoomId(testRoom.getId());
        testMeterReading.setReadingMonth("2024-07");
        testMeterReading.setWaterReading(new BigDecimal("120.00"));
        testMeterReading.setElectricityReading(new BigDecimal("580.00"));
        testMeterReading.setPrevWaterReading(new BigDecimal("100.00"));
        testMeterReading.setPrevElectricityReading(new BigDecimal("480.00"));
        testMeterReading.setWaterUsage(new BigDecimal("20.00"));
        testMeterReading.setElectricityUsage(new BigDecimal("100.00"));
        testMeterReading.setReadingDate(LocalDate.now());
        testMeterReading.setCreatedAt(LocalDateTime.now());
        meterReadingMapper.insert(testMeterReading);
        
        testBill = new Bill();
        testBill.setBillNo("B2024070812345");
        testBill.setRoomId(testRoom.getId());
        testBill.setTenantId(1L);
        testBill.setBillMonth("2024-07");
        testBill.setMeterReadingId(testMeterReading.getId());
        testBill.setRentAmount(new BigDecimal("1500.00"));
        testBill.setWaterAmount(new BigDecimal("50.00"));
        testBill.setElectricityAmount(new BigDecimal("120.00"));
        testBill.setTotalAmount(new BigDecimal("1670.00"));
        testBill.setStatus(1);
        testBill.setPaidAmount(BigDecimal.ZERO);
        testBill.setCreatedAt(LocalDateTime.now());
        billMapper.insert(testBill);
    }
    
    @Test
    void testGetBillPage() throws Exception {
        mockMvc.perform(get("/bills/page")
                .param("current", "1")
                .param("size", "10")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }
    
    @Test
    void testGetBillById() throws Exception {
        mockMvc.perform(get("/bills/{id}", testBill.getId())
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.data.id").value(testBill.getId()))
                .andExpected(jsonPath("$.data.billNo").value("B2024070812345"));
    }
    
    @Test
    void testGenerateBill() throws Exception {
        // 创建新的抄表记录用于生成账单
        MeterReading newReading = new MeterReading();
        newReading.setRoomId(testRoom.getId());
        newReading.setReadingMonth("2024-08");
        newReading.setWaterReading(new BigDecimal("140.00"));
        newReading.setElectricityReading(new BigDecimal("680.00"));
        newReading.setPrevWaterReading(new BigDecimal("120.00"));
        newReading.setPrevElectricityReading(new BigDecimal("580.00"));
        newReading.setWaterUsage(new BigDecimal("20.00"));
        newReading.setElectricityUsage(new BigDecimal("100.00"));
        newReading.setReadingDate(LocalDate.now());
        newReading.setCreatedAt(LocalDateTime.now());
        meterReadingMapper.insert(newReading);
        
        mockMvc.perform(post("/bills/generate/{meterReadingId}", newReading.getId())
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("账单生成成功"));
    }
    
    @Test
    void testCreateBill() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setRoomId(testRoom.getId());
        billDTO.setBillMonth("2024-08");
        billDTO.setRentAmount(new BigDecimal("1500.00"));
        billDTO.setRemark("手动创建的账单");
        
        mockMvc.perform(post("/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(billDTO))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.message").value("账单创建成功"));
    }
    
    @Test
    void testUpdateBill() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setId(testBill.getId());
        billDTO.setRentAmount(new BigDecimal("1600.00"));
        billDTO.setOtherDetails("[{\"name\":\"管理费\",\"amount\":100.00}]");
        
        mockMvc.perform(put("/bills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(billDTO))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.message").value("账单更新成功"));
    }
    
    @Test
    void testDeleteBill() throws Exception {
        mockMvc.perform(delete("/bills/{id}", testBill.getId())
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.message").value("账单删除成功"));
    }
    
    @Test
    void testBatchGenerateBills() throws Exception {
        mockMvc.perform(post("/bills/batch-generate")
                .param("billMonth", "2024-07")
                .param("overwrite", "false")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testMarkBillAsPaid() throws Exception {
        mockMvc.perform(post("/bills/{id}/pay", testBill.getId())
                .param("paidAmount", "1670.00")
                .param("paymentMethod", "wechat")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.message").value("账单支付状态更新成功"));
    }
    
    @Test
    void testVoidBill() throws Exception {
        mockMvc.perform(post("/bills/{id}/void", testBill.getId())
                .requestAttr("userId", 1L))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.message").value("账单作废成功"));
    }
    
    @Test
    void testGetBillStatistics() throws Exception {
        mockMvc.perform(get("/bills/statistics")
                .param("billMonth", "2024-07")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").exists());
    }
    
    @Test
    void testGetBillsByMonth() throws Exception {
        mockMvc.perform(get("/bills/monthly/{billMonth}", "2024-07")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.data").isArray());
    }
    
    @Test
    void testExistsBill() throws Exception {
        mockMvc.perform(get("/bills/exists")
                .param("roomId", testRoom.getId().toString())
                .param("billMonth", "2024-07"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.data").value(true));
    }
}