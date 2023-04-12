package com.kxzhu.timing_food_delivery.config;

import com.kxzhu.timing_food_delivery.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @ClassName WebMvcConfig
 * 项目启动时就会调用这些方法
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-13 09:24
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        //前端发来的请求是什么样的(http://ip:port/backend/xxxx)-->映射到什么地方(类路径resources下的backend下的xxx)
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        log.info("开始进行静态资源映射");
    }

    /**
     * 扩展自己的消息转换器
     * 以便于将Long型的id转为String型，不丢失精度
     * @param converters the list of configured converters to extend
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {//mvc框架默认有几个转换器，放在converters中
        log.info("扩展消息转换器...");
        //创建消息转换器对象。将controller方法的返回结果R对象，转成json，再通过输出流响应给页面
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        //设置对象转换器，底层使用jackson，将java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());//JacksonObjectMapper是粘贴过来的对象映射器

        //将上面的消息转化器对象 追加到mvc框架的转换器集合中
        converters.add(0, messageConverter); //把自定义的转换器追加到converters中，并放在最前面优先用，否则可能会先用别的转换器
    }
}
