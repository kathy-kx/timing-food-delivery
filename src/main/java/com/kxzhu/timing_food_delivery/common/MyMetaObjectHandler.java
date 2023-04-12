package com.kxzhu.timing_food_delivery.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @ClassName MyMetaObjectHandler
 * @Description mybatis提供的公共字段自动填充功能。第二步：
 * 编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口
 * 执行时间：执行insert操作或者update操作的时候，执行insertFill()和updateFill()方法
 * @Author zhukexin
 * @Date 2023-02-18 15:28
 */

@Slf4j
@Component  //让spring框架管理它
public class MyMetaObjectHandler implements MetaObjectHandler {
    //插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        //log.info("公共字段自动填充【insert】。。。");
        //log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now()); //属性名,值
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());

    }

    //更新时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        //log.info("公共字段自动填充【update】。。。");
        //log.info(metaObject.toString());

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
