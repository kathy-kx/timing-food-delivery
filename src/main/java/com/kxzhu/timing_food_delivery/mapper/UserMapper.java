package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName UserMapper
 * @Description 用于管理user表
 * @Author zhukexin
 * @Date 2023-03-10 13:43
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    //BaseMapper是mybatisplus提供的。Mapper继承该接口后，无需编写mapper.xml文件，即可获得CRUD功能
    //需要提供一个泛型，是实体类
}
