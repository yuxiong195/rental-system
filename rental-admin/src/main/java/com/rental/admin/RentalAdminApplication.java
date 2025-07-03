package com.rental.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 房东端管理系统启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.rental.common", "com.rental.admin"})
@MapperScan("com.rental.common.mapper")
@EnableTransactionManagement
public class RentalAdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RentalAdminApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "    房租管理系统 - 房东端启动成功    \n" +
                "    API文档地址: http://localhost:8080/doc.html\n" +
                "========================================\n");
    }
}