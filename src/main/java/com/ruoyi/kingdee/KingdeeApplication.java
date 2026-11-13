package com.ruoyi.kingdee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 金蝶采购收货单 / 采购车辆计划查询工具启动类。
 * Mapper 与数据源的绑定关系由 config 包下的 PrimaryMybatisConfig、VehiclePlanDataSourceConfig 负责。
 */
@SpringBootApplication
public class KingdeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KingdeeApplication.class, args);
    }
}
