package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.dto.DishDto;
import com.kxzhu.timing_food_delivery.entity.Dish;

/**
 * @ClassName DishService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 22:15
 */
public interface DishService extends IService<Dish> {

    /**
     * 向dish和dish_flavor表插入数据
     * 原本dishService中的save()方法，只能操作dish表。我们还需要操作dish_flavor表，添加菜品对应的口味信息
     * @param dishDto
     */
    public void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id，从dish和dish_flavor表中查询菜品信息（含菜品口味）
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id);

    /**
     * 根据菜品的id，将前端用户填写的数据，对dish和dish_flavor表进行修改
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);
}
