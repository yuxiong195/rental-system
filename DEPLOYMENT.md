# 房租管理系统部署指南

## 系统要求

- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

## 快速启动

### 1. 数据库配置

执行SQL初始化脚本：
```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置修改

修改 `rental-admin/src/main/resources/application.yml` 中的数据库和Redis连接信息：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/rental_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
      username: your_username
      password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行房东端管理系统
cd rental-admin
mvn spring-boot:run
```

### 4. 访问系统

- **房东端管理系统**: http://localhost:8080/admin
- **API文档**: http://localhost:8080/admin/swagger-ui.html
- **数据库监控**: http://localhost:8080/admin/druid

## 测试账户

### 房东账户
- 用户名: landlord1
- 密码: 123456

### 租客账户  
- 用户名: tenant1
- 密码: 123456

## API接口

### 认证接口
- POST `/admin/auth/login` - 用户登录
- POST `/admin/auth/register` - 用户注册
- POST `/admin/auth/logout` - 用户退出

### 账单管理接口
- GET `/admin/bills/page` - 分页查询账单
- GET `/admin/bills/{id}` - 获取账单详情
- POST `/admin/bills/generate/{meterReadingId}` - 基于抄表记录生成账单
- POST `/admin/bills` - 手动创建账单
- PUT `/admin/bills` - 更新账单
- DELETE `/admin/bills/{id}` - 删除账单
- POST `/admin/bills/batch-generate` - 批量生成账单
- POST `/admin/bills/{id}/pay` - 标记账单为已支付
- POST `/admin/bills/{id}/void` - 作废账单
- GET `/admin/bills/statistics` - 获取账单统计
- GET `/admin/bills/monthly/{billMonth}` - 获取月度账单
- GET `/admin/bills/exists` - 检查账单是否存在

### 抄表管理接口
- GET `/admin/meter-readings/page` - 分页查询抄表记录
- GET `/admin/meter-readings/{id}` - 获取抄表记录详情
- POST `/admin/meter-readings` - 添加抄表记录
- PUT `/admin/meter-readings` - 更新抄表记录
- DELETE `/admin/meter-readings/{id}` - 删除抄表记录
- POST `/admin/meter-readings/batch` - 批量添加抄表记录
- GET `/admin/meter-readings/latest/{roomId}` - 获取最新抄表记录
- GET `/admin/meter-readings/monthly/{month}` - 获取月度抄表记录
- POST `/admin/meter-readings/{id}/generate-bill` - 生成账单

## 主要功能

### 1. 账单管理
- ✅ 基于抄表记录自动生成账单
- ✅ 手动创建账单
- ✅ 账单CRUD操作
- ✅ 批量生成账单
- ✅ 支付状态管理
- ✅ 账单统计报表

### 2. 抄表管理
- ✅ 抄表记录CRUD操作
- ✅ 自动计算用量
- ✅ 批量抄表功能
- ✅ 抄表图片上传

### 3. 权限控制
- ✅ JWT身份认证
- ✅ 房东数据隔离
- ✅ API权限验证

## 技术栈

- **后端框架**: Spring Boot 3.2.5
- **数据库**: MySQL 8.0 + MyBatis Plus 3.5.6
- **缓存**: Redis
- **连接池**: Druid
- **文档**: SpringDoc OpenAPI 3.0
- **安全**: JWT + Spring Security
- **工具**: Hutool + Lombok

## 测试

### 运行单元测试
```bash
mvn test -pl rental-common
```

### 运行集成测试
```bash
mvn test -pl rental-admin
```

## 开发环境

建议使用以下开发工具：
- **IDE**: IntelliJ IDEA 2023.3+
- **JDK**: Eclipse Temurin 17 LTS
- **数据库工具**: Navicat/DataGrip
- **API测试**: Postman/Swagger UI

## 故障排除

### 数据库连接问题
1. 检查MySQL是否正常运行
2. 验证数据库用户名密码
3. 确认数据库已创建

### Redis连接问题
1. 检查Redis是否启动
2. 验证Redis连接配置
3. 检查防火墙设置

### 应用启动失败
1. 检查端口是否被占用
2. 查看日志文件确定错误原因
3. 验证配置文件格式