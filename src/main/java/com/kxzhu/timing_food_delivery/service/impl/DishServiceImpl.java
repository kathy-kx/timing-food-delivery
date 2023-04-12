package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.dto.DishDto;
import com.kxzhu.timing_food_delivery.entity.Category;
import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.DishFlavor;
import com.kxzhu.timing_food_delivery.mapper.DishMapper;
import com.kxzhu.timing_food_delivery.service.DishFlavorService;
import com.kxzhu.timing_food_delivery.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName DishServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 22:16
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 保存新增的菜品，同时向数据库插入菜品对应的口味数据
     * 需要操作两个表：dish和dish_flavor表
     * 原本dishService中的save()方法，只能操作dish表。我们还需要操作dish_flavor表，添加菜品对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional //涉及多张表，所以使用事务。还需要在主程序加@EnableTransactionManagement，以启用事务
    public void saveWithFlavor(DishDto dishDto) { //传来的dishDto对象封装了Dish的信息，和扩展的flavors（但是这时候刚从前端传来，还没有自动生成菜品id，所以没有Dish类的id属性）

        //保存菜品基本信息到菜品表dish
        this.save(dishDto);//DishDto继承自Dish，所以dishDto中封装了菜品表中所需的信息。但是dishDto扩展的部分还没保存
        //save(dishDto)保存菜品后，雪花算法自动生成菜品的id（Dish类的id属性），插入到数据库。mybatis plus帮我们自动返回主键。

        Long dishId = dishDto.getId();//获得菜品id。DishDto继承自Dish，也有属性id。所以此处可以用getId()获得菜品id。

        //前端传来的"flavors":[{"name":"甜味","value":"[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]"
        //flavors是用List集合盛装DishFlavor对象。前端传来的只有DishFlavor的name和value，还缺DishFlavor类中的dishId
        //此处必须给flavors信息加上dishId数据，否则不知道添加的口味（name和value）是哪个菜的，无法对应。
        //dish_flavor表中的dish_id 对应 dish表中的id。先获取dish表的id（从dishDto获取），再将这个id加到集合中，并插入到dish_flavor表中。

        //此处用流的方式（也可以用for i循环），将信息封装到item中，给item添加上菜品id，再转成集合，重新赋值给flavors这个集合
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map( (item) ->{  //item：遍历出的每一项，对应一个DishFlavor实体
            item.setDishId(dishId); //加工每一个item，把item的属性dishId赋好值
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据 到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);//dishDto中的flavors属性是List类型，saveBatch方法可以批量操作保存集合。
    }

    /**
     * 根据id，从dish表和dish_flavor表中查询菜品信息（含菜品口味）
     * @param id
     * @return
     */
    @Override
    @Transactional //涉及多张表，所以使用事务。还需要在主程序加@EnableTransactionManagement，以启用事务
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);//查出dish表中该id的菜品数据，封装到dish对象（不含菜品口味）
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        //现在dishDto中还差菜品口味。从dish_flavor表中，根据dish_id获取name和value，封装到flavors对象
        //查询菜品口味信息：
        //构造条件构造器 包装查询对象queryWrapper
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件(从哪个表查，以及占位符?具体是什么-这里是id) select * from dish_flavor where dish_id = ?
        queryWrapper.eq( DishFlavor::getDishId, id);
        //调用该表对应的service的方法，查出结果，以List形式封装
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);//查询为列表
        dishDto.setFlavors(list);//将查出的口味结果，赋值给dishDto对象的flavors属性
        return dishDto;
    }

    /**
     * 根据菜品的id，用前端用户填写的数据，对dish和dish_flavor表进行修改
     * @param dishDto
     */
    @Override
    @Transactional //涉及多张表，所以使用事务。还需要在主程序加@EnableTransactionManagement，以启用事务
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表的部分，id对应的name、category_id、price、code、image、description数据
        this.updateById(dishDto);

        //更新dish_flavor表中，dish_id对应的name、value数据
        //根据dish_id，清理当前id对应菜品的口味数据---对dish_flavor表的delete操作

        // 想执行的是：delete from dish_flavor where dish_id = ?
        //为什么不可以用：dishFlavorService.removeById();？——因为removeById用的是dish_flavor的id（主键），此处需要根据"dish_id"来查找
        //构造条件构造器 包装查询对象queryWrapper
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //再添加当前提交的口味数据---对dish_flavor表的insert操作
        //此处用流的方式（也可以用for i循环），将信息封装到item中，给item添加上菜品id，再转成集合，重新赋值给flavors这个集合(和上面的saveWithFlavor方法一样)
        List<DishFlavor> flavors = dishDto.getFlavors();//此时的flavors集合只有name、value这两项，还要加上dish_id才行
        flavors = flavors.stream().map( (item) ->{  //item：遍历出的每一项，对应一个DishFlavor实体
            item.setDishId(dishDto.getId()); //加工每一个item，把item的属性dishId赋好值
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);//把dishDto中取出的flavors数据 保存到dish_service表中

    }


}
