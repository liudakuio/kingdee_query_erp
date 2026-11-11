package com.ruoyi.kingdee;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 金蝶采购收货单查询工具启动类。
 */
@SpringBootApplication
@MapperScan("com.ruoyi.kingdee.mapper")
public class KingdeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KingdeeApplication.class, args);
    }
}
