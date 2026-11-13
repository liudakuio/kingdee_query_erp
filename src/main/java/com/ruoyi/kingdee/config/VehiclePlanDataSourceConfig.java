package com.ruoyi.kingdee.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * 采购车辆计划（独立数据库）的第二数据源配置。
 * 连接信息：kingdee.vehicleplan.datasource.*，真实地址写在本地 application-kingdee.yml，
 * application.yml 中默认指向内置 H2 空库，保证任何 profile 都能启动。
 */
@Configuration
@MapperScan(basePackages = "com.ruoyi.kingdee.mapper.vehicleplan",
        sqlSessionFactoryRef = "vehiclePlanSqlSessionFactory")
public class VehiclePlanDataSourceConfig {

    @Bean("vehiclePlanDataSourceProperties")
    @ConfigurationProperties("kingdee.vehicleplan.datasource")
    public DataSourceProperties vehiclePlanDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("vehiclePlanDataSource")
    @ConfigurationProperties("kingdee.vehicleplan.datasource.hikari")
    public HikariDataSource vehiclePlanDataSource(
            @Qualifier("vehiclePlanDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean("vehiclePlanSqlSessionFactory")
    public SqlSessionFactory vehiclePlanSqlSessionFactory(
            @Qualifier("vehiclePlanDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/vehicleplan/*.xml"));
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 结果集映射为 entity 时，值为 NULL 的列也调用 setter，保证字段状态一致
        configuration.setCallSettersOnNulls(true);
        factory.setConfiguration(configuration);
        return factory.getObject();
    }

    @Bean("vehiclePlanSqlSessionTemplate")
    public SqlSessionTemplate vehiclePlanSqlSessionTemplate(
            @Qualifier("vehiclePlanSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}