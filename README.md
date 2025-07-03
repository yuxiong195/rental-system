# 房租管理系统 (Rental Management System)

轻量级房租管理系统MVP版本，支持房东快速管理房间和租客，自动生成账单。

## 🏗️ 技术栈

- **Java 17** (LTS版本)
- **Spring Boot 3.2.5** (现代化框架)
- **MyBatis Plus 3.5.6** (持久层框架)
- **MySQL 8.0.33** (数据库)
- **Redis** (缓存)
- **JWT** (认证)
- **Knife4j 4.4.0** (API文档)
- **Maven** (构建工具)

## 📁 项目结构

```
rental-system/
├── rental-common/          # 公共模块
│   ├── entity/             # 实体类
│   ├── dto/                # 数据传输对象
│   ├── vo/                 # 视图对象
│   ├── mapper/             # MyBatis映射器
│   ├── service/            # 业务服务层
│   ├── config/             # 配置类
│   ├── exception/          # 异常处理
│   ├── enums/              # 枚举类
│   └── utils/              # 工具类
├── rental-admin/           # 房东端管理系统
│   ├── controller/         # 控制器层
│   └── resources/          # 配置文件
├── rental-web/             # 租客端应用
└── pom.xml                 # 根POM文件
```

## 🚀 核心功能

### 1. 房东认证
- ✅ 房东注册（手机号+密码）
- ✅ 房东登录（密码验证）
- ✅ 租客登录（短信验证码）
- ✅ JWT Token认证
- ✅ 短信验证码发送

### 2. 房间管理
- ✅ 房间CRUD操作
- ✅ 房间状态管理（空置/已出租/维修中）
- ✅ 费用标准配置（租金、水电费单价等）
- ✅ 租客绑定/解绑
- ✅ 批量状态更新
- ✅ 权限验证（房东只能管理自己的房间）

### 3. 抄表管理
- ✅ 抄表记录CRUD操作
- ✅ 批量抄表功能
- ✅ 用量自动计算
- ✅ 水电表照片上传
- ✅ 月度抄表统计
- ✅ 防重复抄表验证

## 🛠️ 开发特性

### 代码质量
- **统一响应格式**: 标准化API响应结构
- **全局异常处理**: 统一的错误处理机制
- **参数校验**: 完整的DTO参数验证
- **详细注释**: 所有类和方法都有完整的中文注释
- **权限控制**: JWT拦截器+业务层权限验证

### 安全特性
- **JWT认证**: 无状态的Token认证
- **密码加密**: BCrypt密码哈希
- **权限验证**: 多层权限检查
- **参数验证**: 严格的输入验证
- **SQL注入防护**: MyBatis防注入

### 性能优化
- **连接池**: Druid数据库连接池
- **缓存**: Redis缓存支持
- **分页查询**: MyBatis Plus分页
- **批量操作**: 支持批量插入和更新

## 📝 API文档

启动应用后访问: `http://localhost:8080/admin/doc.html`

### 主要接口

#### 认证接口
- `POST /admin/auth/register` - 房东注册
- `POST /admin/auth/login` - 用户登录
- `POST /admin/auth/sms/send` - 发送验证码

#### 房间管理
- `GET /admin/rooms/page` - 房间列表（分页）
- `POST /admin/rooms` - 添加房间
- `PUT /admin/rooms` - 更新房间
- `DELETE /admin/rooms/{id}` - 删除房间
- `POST /admin/rooms/{id}/bind-tenant` - 绑定租客

#### 抄表管理
- `GET /admin/meter-readings/page` - 抄表记录列表
- `POST /admin/meter-readings` - 添加抄表记录
- `POST /admin/meter-readings/batch` - 批量抄表
- `GET /admin/meter-readings/latest/{roomId}` - 最新抄表记录

## 🔧 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 启动步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd rental-system
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE rental_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库结构
mysql -u root -p rental_system < rental_system_db.sql
```

3. **修改配置**
```yaml
# rental-admin/src/main/resources/application.yml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/rental_system?...
      username: your_username
      password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

4. **启动应用**
```bash
# 编译项目
mvn clean compile

# 启动房东端
cd rental-admin
mvn spring-boot:run
```

5. **访问应用**
- 房东端管理系统: http://localhost:8080/admin
- API文档: http://localhost:8080/admin/doc.html
- Druid监控: http://localhost:8080/admin/druid/

## 📊 数据库设计

### 核心表结构
- `users` - 用户表（房东+租客）
- `properties` - 房产表
- `rooms` - 房间表（核心业务表）
- `meter_readings` - 抄表记录表
- `bills` - 账单表
- `receipts` - 收据表
- `notifications` - 通知表
- `fee_templates` - 费用模板表

### 关键设计
- **用户统一管理**: 房东和租客在同一个用户表
- **快速绑定**: 通过手机号快速绑定租客
- **自动计算**: 用量和费用自动计算
- **数据完整性**: 外键约束和触发器保证数据一致性

## 🔄 业务流程

### 快速出租流程
1. 房东添加房产和房间
2. 设置费用标准
3. 输入租客手机号绑定
4. 系统自动创建租客账号

### 月度抄表流程
1. 房东批量抄表录入
2. 系统自动计算用量
3. 基于抄表记录生成账单
4. 租客收到账单通知

## 📈 扩展计划

- [ ] 账单自动生成和推送
- [ ] 微信小程序端
- [ ] 在线支付集成
- [ ] 数据报表和统计
- [ ] 消息推送服务
- [ ] 合同管理功能

## 👥 开发团队

- **架构设计**: Claude Code Assistant
- **业务逻辑**: 基于实际租房管理需求
- **技术选型**: 企业级主流技术栈

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---
*最后更新: 2024-07-03*