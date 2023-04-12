package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName DishMapper
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 22:14
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
