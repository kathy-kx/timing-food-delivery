package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName SetmealDishMapper
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-06 22:20
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish>{
    //BaseMapper是mybatisplus提供的。Mapper继承该接口后，无需编写mapper.xml文件，即可获得CRUD功能
    //需要提供一个泛型，是实体类
}
