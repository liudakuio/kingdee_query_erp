package com.ruoyi.kingdee.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 演示档案专用：在内置 H2 车辆计划库里建表并灌入示例数据，
 * 使演示模式下也能完整演示采购车辆计划查询页面。
 * 真实金蝶库不执行任何建表脚本，表结构由 DBA 提供。
 */
@Configuration
@Profile("demo")
public class VehiclePlanDemoDataConfig {

    /** 脚本统一按 UTF-8 读取，避免平台默认编码导致中文乱码 */
    private static final String SCRIPT_ENCODING = "UTF-8";

    private final DataSource dataSource;

    public VehiclePlanDemoDataConfig(@Qualifier("dataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public InitializingBean vehiclePlanDemoDataInitializer() {
        return () -> {
            // dynamic-datasource 的 dataSource() Bean 声明类型为 DataSource，这里按路由实现取命名数据源
            DataSource vehiclePlanDataSource = ((DynamicRoutingDataSource) dataSource).getDataSource("vehicleplan");
            try (Connection connection = vehiclePlanDataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, script("db/vehicleplan-schema.sql"));
                ScriptUtils.executeSqlScript(connection, script("db/vehicleplan-data.sql"));
            } catch (Exception e) {
                throw new IllegalStateException("车辆计划演示数据初始化失败", e);
            }
        };
    }

    private EncodedResource script(String path) {
        return new EncodedResource(new ClassPathResource(path), SCRIPT_ENCODING);
    }
}