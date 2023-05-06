package com.kxzhu.timing_food_delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ServletComponentScan   //  加上这个注解后，才会扫描@WebFilter注解，使得过滤器生效
@Slf4j  //加上这个注解后，可以在主程序中使用log.xxxx方法，便于打印日志
@SpringBootApplication
@EnableTransactionManagement //和@Transactional一起，开启事务
@EnableCaching //开启缓存注解功能
public class TimingFoodDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimingFoodDeliveryApplication.class, args);
        log.info("项目启动成功");
    }

}
