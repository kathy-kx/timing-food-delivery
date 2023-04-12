package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.Category;
import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.Setmeal;
import com.kxzhu.timing_food_delivery.common.CustomException;
import com.kxzhu.timing_food_delivery.mapper.CategoryMapper;
import com.kxzhu.timing_food_delivery.service.CategoryService;
import com.kxzhu.timing_food_delivery.service.DishService;
import com.kxzhu.timing_food_delivery.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName CategoryServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 17:02
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;//需要查菜品表中的信息，调用dishService的方法

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类
     * 之前只是调用mybatis plus提供的功能，直接进行删除，但是没有检查：删除的分类是否关联了菜品或者套餐，
     * 所以自己写，进行功能完善。在删除之前，先判断：要删除的分类是否已经关联了菜品/套餐，即Dish/Setmeal表中的某一条数据是否使用了该分类
     * 用sql查的话就是：
     * select count(*) from dish where category_id = ?
     * 其中?是当前要删除的分类的category_id，即remove(Long id)的形参
     *
     * @param id
     */
    @Override
    public void remove(Long id) {

        //一、查询当前要删除的分类是否已经关联【菜品】，如已关联，抛异常
        /*
        1、先写希望执行的sql语句：SELECT COUNT( * ) FROM dish WHERE (category_id = ?)
        2、组装查询条件+具体查询
        查询条件：queryWrapper.eq等值查询(哪个表::根据谁查, 具体的值); 相当于 FROM dish WHERE category_id = ? 占位符?用具体的值id填充
        具体查询：dishService.count(queryWrapper); 相当于SELECT COUNT( * )
        3、参数需要用到queryWrapper，即条件构造器，需要先声明
        用法：LambdaQueryWrapper<要查的表对应的实体类> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
         */
        //构造条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        //具体查询
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //如已关联，抛异常
        if (count1 > 0){//如果查出来发现，dish表中至少有1条菜品数据的category_id = 形参id，说明待删除分类已经关联菜品
            throw new CustomException("当前分类已关联菜品，不能删除");//还需要在全局异常处理器GlobalExceptionHandler中，捕获次业务异常
        }

        //二、查询当前要删除的分类是否已经关联【套餐】，如已关联，抛异常。步骤同上
        //构造条件构造器：
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);

        //如已关联，抛异常
        if (count2 > 0){//如果查出来发现，setmeal表中至少有1条套餐数据的category_id = 形参id，说明待删除分类已经关联套餐
            throw new CustomException("当前分类已关联套餐，不能删除");
        }

        //如果都没关联，可以正常删除
        super.removeById(id); //原来mybatis plus提供的功能

    }
}
