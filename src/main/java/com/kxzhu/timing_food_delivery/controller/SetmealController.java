package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.dto.DishDto;
import com.kxzhu.timing_food_delivery.dto.SetmealDto;
import com.kxzhu.timing_food_delivery.entity.Category;
import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.Setmeal;
import com.kxzhu.timing_food_delivery.service.CategoryService;
import com.kxzhu.timing_food_delivery.service.SetmealDishService;
import com.kxzhu.timing_food_delivery.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName SetmealController
 * @Description 套餐管理
 * 虽然对套餐的管理需要操作setmeal表和setmeal_dish表两个表，但操作的主表 还是套餐表（setmeal表），所以只创建SetmealController即可
 * @Author zhukexin
 * @Date 2023-03-06 22:24
 */
@Slf4j
@RestController //@ResponseBody + @Controller
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 保存(新增)套餐
     * 前端以json形式发送请求给/setmeal，method=post，需要服务器保存套餐相关数据，并返回响应。
     *
     * json数据包含：setmeal中的属性category_id, name, price, status, code, description, image
     * 和setmeal中没有的setmealDishes属性，所以形参要用SetmealDto来接收。SetmealDto类中有setmealDishes属性
     *
     * （以下由service实现）
     * 需要把这些Setmeal类中的属性category_id, name, price, status, code, description, image 保存到setmeal表；
     * 由框架生成该setmeal的id字段（该套餐的id）之后，
     * 再把属性setmeal_id(setmeal表的主键id), category_id, dish_id,name, price, copies保存到setmeal_dish表
     *
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息: {}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * 处理请求/setmeal/page?page=1&pageSize=10&name=xxxx，并响应分页信息pageInfo给前端
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        /*
        返回类型：R，泛型是Page。因为需要传给浏览器records、total数据，以显示分页页面（见list.html）
        参数：请求中带着参数(http://localhost:8080/setmeal/page?page=1&pageSize=10&name=xxxx)发过来，声明和请求参数同名的形参
        跳页码时，请求参数带着page和pageSize；在搜索框搜索套餐名称时，还另外带着name请求参数
        */
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();//参考return R.success(pageInfo)。方法结束时，需要响应一个Page类型的分页构造器对象。
        // 且该对象中含有"分类名称categoryName"，所以还需要构造一个SetmealDto实体对应的Page对象，否则查不到"套餐分类"

        //条件构造器
        //包装查询对象queryWrapper
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件（模糊查询）：只要name不为空，就在setmeal表中 根据name字段查询，要查的值是传来的参数name
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);//like方法的参数：执行条件，字段，值。相当于：LIKE '%值%'
        //添加排序条件：按照Dish表里的更新时间 倒序排列，即最近更新的排在前面(orderBy)
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询（用setmealService，查setmeal表）
        setmealService.page(pageInfo, queryWrapper); //处理好又会封装到Page类型的pageInfo中，pageInfo中就有值了，含有数据列表records、总记录数total等信息

        //原先：return R.success(pageInfo);//此分页对象是关于setmeal的，setmeal表中的套餐分类只是category_id，我们页面需要的是分类名字，无法展示"套餐分类"。
        // 所以此处不能直接返回。需要改造，额外返回category_id对应的分类名categoryName。

        //对象拷贝：setmealDtoPage构建后没有赋值，如何赋值？
        //（1）可以直接把 从setmeal表中查出的pageInfo各属性值 拷贝给setmealDtoPage（除records）
        //（2）再把records转换为SetmealDto类型的：添加上categoryName，设置给setmealDtoPage
        //第（1）步：
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");//不拷贝records属性（页面上的数据信息就是records，需要处理）
        //格式：BeanUtils.copyProperties(源, 目的, 拷贝什么属性);

        //第（2）步：处理records：
        List<Setmeal> records = pageInfo.getRecords();//把pageInfo里的records部分拿出来。
        // 需要处理该List集合：原本是List集合盛装Setmeal，即List<Setmeal>类型的records，不含分类名称；
        // 需要变成List集合盛装SetmealDto，即List<SetmealDto>类型的records，含分类名称。
        // 然后设置setmealDtoPage的records为新的集合list：setmealDtoPage.setRecords(list);
        List<SetmealDto> list = records.stream().map((item)->{ //item：遍历出的每一个菜品对象setmeal
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);//把setmeal的普通属性 copy给 我们自己创建的setmealDto对象
            Long categoryId = item.getCategoryId();//获取当前遍历到的 setmeal对象的 categoryId
            //根据id，从数据库查询分类对象，以获取分类名称
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();//获取分类名称
                setmealDto.setCategoryName(categoryName);//把categoryName赋值给 我们自己创建的setmealDto对象的 categoryName属性
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);//把加了categoryName的新list 赋值给setmealDtoPage的records属性

        return R.success(setmealDtoPage);  //响应回的数据必须含有"分类名称"，封装到Page类型的setmealDtoPage对象中返回

    }

    /**
     * 删除套餐（支持批量）
     * 处理请求/setmeal?ids=xxx
     * 注意：只能删除停售(status=0)的套餐，启售(status=1)的套餐不能删除
     * 所以要先判断套餐的status，如果为0，执行删除。如果为1，返回提示信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String[] ids){
        int count = 0; //计数：ids中有多少个启售的套餐
        for(String id : ids){
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal.getStatus() == 0){//停售(status=0)的套餐
                setmealService.removeById(id); //删除选中的停售套餐
            } else { //启售(status=1)的套餐
                count ++; //启售的套餐数量加1
            }
        }
        if(count > 0 && count == ids.length){//全是启售的
            return R.error("选中的套餐均为'启售'状态，不可删除");
        }else {//如果有至少1个套餐是停售的，删除了选中的停售套餐
            return R.success("套餐删除成功");
        }
    }

    /**
     * 启售停售切换（支持批量）处理/setmeal/status/0?ids=xxx
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> switchStatus(@PathVariable Integer status, String[] ids){
        for (String id : ids){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("状态修改成功");
    }

    /**
     * 修改某个套餐时，需要回显该套餐id的信息
     * 请求是/setmeal/1633386294555971586
     * 需要响应给前端：套餐的id, 套餐分类categoryId, 套餐name, 套餐价格price, 套餐状态status, code, description, image, 公共字段 ————都包含在setmeal对象中
     * 和setmealDishes（集合，盛装多个setmealDish对象，其中含有id, setmealId, 菜品dishId, 菜品name, 菜品price, copies, sort, 公共字段）————须由setmealDto对象来装
     *
     * @param id 套餐的id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> querySetmeal(@PathVariable Long id){//此处id是套餐的id
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * 处理json请求/setmeal，请求方式PUT。
     * 需要服务器将新的信息保存到数据表。
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        //log.info("setmealDto: {}", setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 显示套餐信息
     * 处理请求/setmeal/list?categoryId=1413342269393674242&status=1
     * 形参：用Setmeal实体类接收，含categoryId和status属性
     * @param setmeal
     * @return
     */
    @GetMapping("list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //从setmeal表中查该分类id对应的套餐数据
        //select * from setmeal where category_id = ? and status = ?
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId() );
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
