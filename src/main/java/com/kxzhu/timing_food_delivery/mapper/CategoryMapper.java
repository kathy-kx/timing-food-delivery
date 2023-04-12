package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName CatagoryMapper
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 17:00
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    //BaseMapper是mybatisplus提供的。Mapper继承该接口后，无需编写mapper.xml文件，即可获得CRUD功能
}
