package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.dto.SetmealDto;
import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.Setmeal;
import com.kxzhu.timing_food_delivery.entity.SetmealDish;
import com.kxzhu.timing_food_delivery.mapper.DishMapper;
import com.kxzhu.timing_food_delivery.mapper.SetmealMapper;
import com.kxzhu.timing_food_delivery.service.DishService;
import com.kxzhu.timing_food_delivery.service.SetmealDishService;
import com.kxzhu.timing_food_delivery.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SetmealServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 22:17
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时保存与菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //先把setmeal表中需要的数据填上
        //将json发来的 该套餐的category_id, name, price, status, code, description, image（都封装在setmealDto中）保存到setmeal表；
        this.save(setmealDto);//即setmealDishService.save(setmealDto)
        //执行save之后，框架会生成该setmeal的主键id字段（该套餐的id）

        //将setmeal_dish表中需要的数据填上(保存套餐和菜品的关联信息)
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//获取传来的setmealDto中的setmealDishes属性的值，是集合装了多个setmealDish(每个套餐id对应哪些菜品)。每个setmealDish对象（传来的）中有copies, dishId, name, price数据
        //所以传来的List中的每个setmealDish对象，只差setmeal_id和sort。其中setmeal_id都是刚才框架生成的套餐主键id。（多个setmealDish对象共享一个套餐主键id，都在同一个套餐里）
        setmealDishes = setmealDishes.stream().map((item) ->{//每个item是setmealDish对象
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //将封装的setmealDishes 保存到setmeal_dish表中
        setmealDishService.saveBatch(setmealDishes);

    }

    /**
     * 根据id，查询套餐（含套餐内的菜品）
     * 需要响应给前端：套餐的id, 套餐分类categoryId, 套餐name, 套餐价格price, 套餐状态status, code, description, image, 公共字段 ————都包含在setmeal对象中
     * 和setmealDishes（集合，盛装多个setmealDish对象，其中含有id, setmealId, 菜品dishId, 菜品name, 菜品price, copies, sort, 公共字段）————须由setmealDto对象来装
     * @param id 套餐setmeal的id
     * @return
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);//包含了：套餐的id, 套餐分类categoryId, 套餐name, 套餐价格price, 套餐状态status, code, description, image, 公共字段
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        //copy后，setmealDto中还差setmealDishes属性。可以去setmeal_dish表中，根据setmeal_id查出相应的多个setmeal_dish对象，组成集合
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //select * from setmeal_dish where setmeal_id = ?
        queryWrapper.eq(SetmealDish::getSetmealId, id);//查出相应的多个setmeal_dish对象
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 修改套餐信息setmeal，并更新套餐包含什么菜品(setmeal_dishes)
     * 需要把setmealDto的数据 更新到setmeal表和setmeal_dish表中
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表的基本信息
        this.updateById(setmealDto);//（参数传入entity） 通过setmealService的方法，将setmealDto封装的数据 更新到setmeal表

        //现在还需要把setmealDto中的setmealDishes部分（List<SetmealDish>），更新到setmeal_dish表中

        //更新setmeal_dish表信息:先delete，再insert
        //(1) delete: sql语句 delete from setmeal_dish where setmeal_id = ?
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());//setmealDto.getId()就是当前套餐setmeal的id，在setmeal表中是id字段，在setmeal_dish表中是setmeal_id字段
        setmealDishService.remove(queryWrapper);

        //(2) insert：将setmealDto的setmealDishes中的数据 重新插入setmeal_dish表中（此时why缺少setmeal_id??）
        // 将setmeal_id 添加进setmealDto中的一个个setmealDishes，形成关联
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//缺少setmeal_id???

        setmealDishes = setmealDishes.stream().map((item) -> {//每个item就是一个setmealDish对象，一个套餐的setmealDishes中有多个setmealDish对象
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);//将setmealDto中的setmealDishes部分，更新到setmeal_dish表中
    }
}
