package com.ruoyi.kingdee.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 统一配置。
 *
 * <p>多数据源由 dynamic-datasource 的 @DS 切换，不再按 Mapper 包绑定不同 SqlSessionFactory；
 * 因此这里只保留一个全局 MapperScan 和分页插件。</p>
 */
@Configuration
@MapperScan("com.ruoyi.kingdee.mapper")
public class MybatisPlusConfig {

    /**
     * 分页插件：主表通过 Page + QueryWrapper 控制返回行数，子表仍用 RowBounds 限制。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
