-- 房租管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS rental_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rental_system;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    name VARCHAR(100) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1-房东 2-租客',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记'
) COMMENT '用户表';

-- 房产表
CREATE TABLE IF NOT EXISTS properties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房产ID',
    landlord_id BIGINT NOT NULL COMMENT '房东ID',
    name VARCHAR(200) NOT NULL COMMENT '房产名称',
    address VARCHAR(500) NOT NULL COMMENT '详细地址',
    description TEXT COMMENT '房产描述',
    total_rooms INT DEFAULT 0 COMMENT '总房间数',
    available_rooms INT DEFAULT 0 COMMENT '可用房间数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (landlord_id) REFERENCES users(id)
) COMMENT '房产表';

-- 房间表
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    tenant_id BIGINT COMMENT '租客ID',
    room_name VARCHAR(100) NOT NULL COMMENT '房间名称',
    room_type VARCHAR(50) COMMENT '房间类型',
    area DECIMAL(8,2) COMMENT '房间面积',
    monthly_rent DECIMAL(10,2) NOT NULL COMMENT '月租金',
    deposit DECIMAL(10,2) COMMENT '押金',
    water_price DECIMAL(6,2) COMMENT '水费单价',
    electricity_price DECIMAL(6,2) COMMENT '电费单价',
    cleaning_fee DECIMAL(8,2) COMMENT '卫生费',
    other_fees TEXT COMMENT '其他费用JSON',
    last_water_reading DECIMAL(10,2) COMMENT '最新水表读数',
    last_electricity_reading DECIMAL(10,2) COMMENT '最新电表读数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-空置 2-已出租 3-维修中',
    lease_start_date DATE COMMENT '租赁开始日期',
    lease_end_date DATE COMMENT '租赁结束日期',
    description TEXT COMMENT '房间描述',
    images TEXT COMMENT '房间图片JSON数组',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (property_id) REFERENCES properties(id),
    FOREIGN KEY (tenant_id) REFERENCES users(id)
) COMMENT '房间表';

-- 抄表记录表
CREATE TABLE IF NOT EXISTS meter_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '抄表记录ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    reading_month VARCHAR(7) NOT NULL COMMENT '抄表月份(YYYY-MM)',
    water_reading DECIMAL(10,2) NOT NULL COMMENT '水表读数',
    electricity_reading DECIMAL(10,2) NOT NULL COMMENT '电表读数',
    prev_water_reading DECIMAL(10,2) COMMENT '上期水表读数',
    prev_electricity_reading DECIMAL(10,2) COMMENT '上期电表读数',
    water_usage DECIMAL(10,2) COMMENT '水用量',
    electricity_usage DECIMAL(10,2) COMMENT '电用量',
    reading_date DATE NOT NULL COMMENT '抄表日期',
    images TEXT COMMENT '抄表图片JSON数组',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    UNIQUE KEY uk_room_month (room_id, reading_month)
) COMMENT '抄表记录表';

-- 账单表
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
    bill_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账单编号',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    tenant_id BIGINT NOT NULL COMMENT '租客ID',
    meter_reading_id BIGINT COMMENT '抄表记录ID',
    bill_month VARCHAR(7) NOT NULL COMMENT '账单月份(YYYY-MM)',
    rent_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '租金',
    water_amount DECIMAL(10,2) DEFAULT 0 COMMENT '水费',
    electricity_amount DECIMAL(10,2) DEFAULT 0 COMMENT '电费',
    cleaning_amount DECIMAL(10,2) DEFAULT 0 COMMENT '卫生费',
    other_details TEXT COMMENT '其他费用明细JSON',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    paid_amount DECIMAL(10,2) DEFAULT 0 COMMENT '已支付金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待支付 2-已支付 3-已作废',
    payment_method VARCHAR(50) COMMENT '支付方式',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    due_date DATE COMMENT '应付日期',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (tenant_id) REFERENCES users(id),
    FOREIGN KEY (meter_reading_id) REFERENCES meter_readings(id),
    UNIQUE KEY uk_room_month (room_id, bill_month)
) COMMENT '账单表';

-- 收据表
CREATE TABLE IF NOT EXISTS receipts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收据ID',
    receipt_no VARCHAR(50) NOT NULL UNIQUE COMMENT '收据编号',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '收款金额',
    payment_method VARCHAR(50) NOT NULL COMMENT '支付方式',
    payment_account VARCHAR(100) COMMENT '支付账户',
    receipt_date DATE NOT NULL COMMENT '收款日期',
    remark VARCHAR(500) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (bill_id) REFERENCES bills(id)
) COMMENT '收据表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) COMMENT '消息类型',
    related_id BIGINT COMMENT '关联业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT '消息通知表';

-- 费用模板表
CREATE TABLE IF NOT EXISTS fee_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    landlord_id BIGINT NOT NULL COMMENT '房东ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    rent_amount DECIMAL(10,2) COMMENT '租金',
    water_price DECIMAL(6,2) COMMENT '水费单价',
    electricity_price DECIMAL(6,2) COMMENT '电费单价',
    cleaning_fee DECIMAL(8,2) COMMENT '卫生费',
    other_fees TEXT COMMENT '其他费用JSON',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模板',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    FOREIGN KEY (landlord_id) REFERENCES users(id)
) COMMENT '费用模板表';

-- 创建索引
CREATE INDEX idx_properties_landlord ON properties(landlord_id);
CREATE INDEX idx_rooms_property ON rooms(property_id);
CREATE INDEX idx_rooms_tenant ON rooms(tenant_id);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_meter_readings_room ON meter_readings(room_id);
CREATE INDEX idx_meter_readings_month ON meter_readings(reading_month);
CREATE INDEX idx_bills_room ON bills(room_id);
CREATE INDEX idx_bills_tenant ON bills(tenant_id);
CREATE INDEX idx_bills_month ON bills(bill_month);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_receipts_bill ON receipts(bill_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_fee_templates_landlord ON fee_templates(landlord_id);

-- 插入测试数据
INSERT INTO users (username, password, name, phone, role) VALUES 
('landlord1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2oSTXL2Fk0OPa', '张房东', '13800138001', 1),
('tenant1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2oSTXL2Fk0OPa', '李租客', '13800138002', 2);

INSERT INTO properties (landlord_id, name, address) VALUES 
(1, '阳光小区1号楼', '北京市朝阳区阳光小区1号楼');

INSERT INTO rooms (property_id, room_name, monthly_rent, water_price, electricity_price, cleaning_fee) VALUES 
(1, 'A101', 1500.00, 2.50, 1.20, 50.00),
(1, 'A102', 1600.00, 2.50, 1.20, 50.00);