package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.DishFlavor;
import com.kxzhu.timing_food_delivery.mapper.DishFlavorMapper;
import com.kxzhu.timing_food_delivery.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @ClassName DishFlavorServiceImpl
 * @Description TODO
 * 向dish和dish_flavor表插入数据
 * 原本dishService中的save()方法，只能操作dish表。我们还需要操作dish_flavor表，添加菜品对应的口味信息
 *
 * @Author zhukexin
 * @Date 2023-02-27 18:49
 */
@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
