package com.kxzhu.timing_food_delivery.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName MybatisPlusConfig
 * @Description
 * @Author zhukexin
 * @Date 2023-02-16 16:47
 */
@Configuration
public class MybatisPlusConfig {

    //配置MybatisPlus的分页插件：
    @Bean //让spring管理它
    public MybatisPlusInterceptor mybatisPlusInterceptor(){ //配置MybatisPlusInterceptor类型的bean
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());//加入拦截器。PaginationInnerInterceptor用于分页
        return mybatisPlusInterceptor;
    }
}
