package com.ruoyi.kingdee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 金蝶采购收货单 / 采购车辆计划查询工具启动类。
 * 持久层使用 MyBatis-Plus，两个数据源由 dynamic-datasource 统一管理，
 * 具体路由由 Service 层的 @DS 注解决定。
 */
@SpringBootApplication
public class KingdeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KingdeeApplication.class, args);
    }
}
