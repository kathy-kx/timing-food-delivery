package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.dto.DishDto;
import com.kxzhu.timing_food_delivery.entity.Category;
import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.DishFlavor;
import com.kxzhu.timing_food_delivery.service.CategoryService;
import com.kxzhu.timing_food_delivery.service.DishFlavorService;
import com.kxzhu.timing_food_delivery.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName DishController
 * @Description 菜品管理相关的功能
 * 处理dish表时，需要同时操作dish_flavor表
 * @Author zhukexin
 * @Date 2023-02-27 18:51
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){// 接收浏览器提交表单时，传到服务器的json数据。传来的是json数据，接收时需要加@RequestBody
        /*
        问题：浏览器传来的json数据中，name、price、code、image、description、status、categoryId这些属性，在Dish实体类中都有属性与之对应
        但是flavors，Dish类中没有该属性，无法在参数中用实体类Dish+@RequestBody注解的方式接收。
        解决办法：可以封装另一个类DishDTO，可以接收到所有的这些参数。
         */
        //log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);//saveWithFlavor方法需要自己写

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * 处理请求：/dish/page?page=1&pageSize=10 或者http://localhost:8080/dish/page?page=1&pageSize=10&name=xxxx
     * 发送需要展示在""页面上的分页数据，含菜品名称、图片（名称）、菜品分类、售价、售卖状态、最后更新时间、操作
     * 其中，菜品名称、图片（名称）、售价、售卖状态、最后更新时间、操作，可以通过LambdaQueryWrapper<Dish>，从dish表中查出来，
     * 再通过dishService.page(pageInfo, queryWrapper)，由Page<Dish>类型的pageInfo对象的 records属性封装这些数据。最后通过R.success(pageInfo)传回前端。
     * 但是由于都是从dish表中查出的，没有"category_name"(菜品分类)这一项，只有"category_id"，所以需要处理records，使之包含"category_name"。
     * （1）新建Page<DishDto>类型的dishDtoPage对象，由于DishDto包含category_name属性，该对象的records属性可以包含"category_name"字段数据
     * （2）将刚才从dish表查出的数据（已经封装到pageInfo对象中）复制到dishDtoPage对象中，先不复制records属性。BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
     * （3）给records中加上"category_name"字段的数据。
     * （3.1）把刚才从dish表中查出的 pageInfo里的records属性 的值取出来。该值是List<Dish>类型，且不含"category_name"
     * （3.2）需要将records变成：List集合盛装DishDto，含category_name。具体如下：
     *         records是List类型（盛装Dish对象），那就可以用流来表示。records.stream()将List类型的records转换为流，流中有多个Dish对象。
     *         重写接口方法map()？？?，遍历流中的每一个Dish对象，称为item。
     *         我们想让流中存放的一个个对象中，不仅含dish表中查出的数据，还含category表中查出的category_name数据。于是在DishDto类中添加categoryName属性，以便封装category_name数据。
     *         流中的每一个dish对象 我们可以换成一个dishDto对象，其中存有dish的属性值，还另存了category_name属性值
     *         所以对于每个item，我们新造一个dishDto对象去替换原来的dish对象。将item（dish对象）的属性值copy给新造的dishDto对象。（此时dishDto中除了categoryName属性，其他都有了）
     *         接下来去数据库查分类名称：通过item.getCategoryId()获取dish对象中的category_id属性值，用这个id去category表中查出一个category对象，再获取该category对象的name属性值
     *         将获取到的categoryName赋值给dishDto对象的categoryName属性。
     *         最后将dishDto对象return回map()接口方法，则流中都是dishDto对象了。
     *         最后使用collect(Collectors.toList())方法，将流转换回List集合，重新赋值给records。这样records中装的就是DishDto对象，包含categoryName数据
     * （3.3）把dishDtoPage对象的records属性 设置为新的List<DishDto>集合list：dishDtoPage.setRecords(list);
     * （4）把换过的records复制给dishDtoPage对象，最后通过R.success(dishDtoPage)返回给前端。
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        /*
        返回类型：R，泛型是Page。因为需要传给浏览器records、total数据，以显示分页页面（见list.html）
        参数：请求中带着参数(http://localhost:8080/dish/page?page=1&pageSize=10&name=xxxx)发过来，声明和请求参数同名的形参
        跳页码时，请求参数带着page和pageSize；在搜索框搜索菜品名称时，还另外带着name请求参数
        */

        //分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();//参考return R.success(pageInfo)。方法结束时，需要响应一个Page类型的分页构造器对象。
        // 且该对象中含有"分类名称categoryName"，所以还需要构造一个DishDto实体对应的Page对象

        //条件构造器
        //包装查询对象queryWrapper
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件（模糊查询）：只要name不为空，就在Dish表中 根据name字段查询，要查的值是传来的参数name
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);//like方法的参数：执行条件，字段，值。相当于：LIKE '%值%'
        //添加排序条件：按照Dish表里的更新时间 倒序排列，即最近更新的排在前面(orderBy)
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询（用dishService，查dish表）
        dishService.page(pageInfo, queryWrapper); //处理好会封装到Page类型的pageInfo中，pageInfo中含有数据列表records、总记录数total等信息

        //原先：return R.success(pageInfo);//此分页对象是关于dish的，dish表中的菜品分类只是category_id，我们页面需要的是分类名字，无法展示"菜品分类"。
        // 所以此处不能直接返回。需要改造，额外返回category_id对应的分类名categoryName。查看list.html也能看出，需要后端响应名为"categoryName"的字段，才能显示"菜品分类"这一项


        //对象拷贝：dishDtoPage构建后没有赋值，如何赋值？
        //（1）可以直接把 从dish表中查出的pageInfo各属性值 拷贝给dishDtoPage（除records）
        //（2）再把records转换为DishDto类型的，添加上categoryName，设置给dishDtoPage
        //第（1）步：
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");//不拷贝records属性（页面上的数据信息就是records，需要处理）
        //格式：BeanUtils.copyProperties(源, 目的, 拷贝什么属性);

        //第（2）步：处理records：
        List<Dish> records = pageInfo.getRecords(); //把pageInfo里的records属性的值拿出来。
        // 思路：需要处理该List集合：原本是List集合盛装Dish，List<Dish>类型的records，不含category_name；需要变成List集合盛装DishDto，即List<DishDto>类型的records，含category_name。
        // 把dishDtoPage对象的records属性 设置为新的List<DishDto>集合list：dishDtoPage.setRecords(list);
        /*
        records是List类型（盛装Dish对象），那就可以用流来表示。records.stream()将List类型的records转换为流，流中有多个Dish对象。
        重写接口方法map()？？?，遍历流中的每一个Dish对象，称为item。
        我们想让流中存放的一个个对象中，不仅含dish表中查出的数据，还含category表中查出的category_name数据。于是在DishDto类中添加categoryName属性，以便封装category_name数据。
        流中的每一个dish对象 我们可以换成一个dishDto对象，其中存有dish的属性值，还另存了category_name属性值
        所以对于每个item，我们新造一个dishDto对象去替换原来的dish对象。将item（dish对象）的属性值copy给新造的dishDto对象。（此时dishDto中除了categoryName属性，其他都有了）
        接下来去数据库查分类名称：通过item.getCategoryId()获取dish对象中的category_id属性值，用这个id去category表中查出一个category对象，再获取该category对象的name属性值
        将获取到的categoryName赋值给dishDto对象的categoryName属性。
        最后将dishDto对象return回map()接口方法，则流中都是dishDto对象了。
        最后使用collect(Collectors.toList())方法，将流转换回List集合，重新赋值给records。这样records中装的就是DishDto对象，包含categoryName数据
         */
        List<DishDto> list=records.stream().map((item)->{ //item：遍历出的每一个菜品对象dish
            DishDto dishDto=new DishDto();

            BeanUtils.copyProperties(item, dishDto);//把dish的普通属性 copy给 我们自己创建的dishDto对象
            Long categoryId = item.getCategoryId();//获取当前遍历到的 dish对象的 categoryId
            //根据id，从数据库查询分类对象，以获取分类名称
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();//获取分类名称
                dishDto.setCategoryName(categoryName);//把categoryName赋值给 我们自己创建的dishDto对象的 categoryName属性
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);//把加了categoryName的新list 赋值给dishDtoPage的records属性

        return R.success(dishDtoPage);  //响应回的数据必须含有"分类名称"，封装到Page类型的dishDtoPage对象中返回
    }

    /**
     * 菜品修改-查询id对应的菜品信息（封装到dishDto对象），以回显到前端修改页面
     * 处理前端发来的GET请求/dish/{id}
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> queryDish(@PathVariable Long id){
        //Dish dish = dishService.getById(id);//这样只能查到dish表中的数据，还需要改为dishDto，查到dish_flavor表中的数据，才能显示菜品口味
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息。处理前端发来的PUT请求/dish（JSON请求）
     * 为什么不能复用save():如果仅仅是保存，则填写后会新增一条数据。而我们需要根据id，找到表里这条数据，对它进行修改，所以需要重新写一个sercice方法
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("菜品修改成功");
    }

    /**
     * 修改启售/停售状态，支持批量修改和单独修改
     * 处理请求/dish/status/1?ids=1413384757047271425,1413385247889891330
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> switchStatus(@PathVariable int status, String[] ids){
        for(String id : ids){
            Dish dish = dishService.getById(id);//通过遍历到的这个当前id，获取dish表中的一条记录（一个dish对象）
            dish.setStatus(status);//将该记录的status改为url路径参数中的status
            dishService.updateById(dish);
        }
        return R.success("状态修改成功");
    }

    /**
     * 删除菜品
     * 请求/dish?ids=1630924768242917378
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String[] ids){
        for (String id : ids){
            dishService.removeById(id);
        }
        return R.success("菜品删除成功");
    }

    /**
     * 根据分类id, 查出该分类的所有菜品
     * 返回值：由于查出的是多个菜品dish，所以用List集合
     * 处理请求：/dish/list?categoryId=1397844263642378242
     * 后期改造：见下一个方法。
     * 此处注释掉原来的写法
     * @param dish
     * @return
     */
    //@GetMapping("/list")
    //public R<List<Dish>> list(Dish dish){
    //    //参数：为什么传来的请求带参数，但是形参用Dish接收？
    //    //用 (Long categoryId)来接收也可以
    //    //也可以用实体类Dish dish接收，通用性更强，易于扩展。
    //    // 只要实体类Dish中的属性名categoryId 和 传输过来的请求参数 name名字("category_id")一致，就可以通过 实体类类型的形参 获取请求参数
    //
    //    //从表中查询所有的分类：select * from dish where category_id = ? AND status = ?
    //    //构造条件构造器：
    //    LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
    //    //添加条件：根据category_id查询
    //    //具体条件 用::的写法 Dish::getCategoryId。具体的值 是传过来的参数dish.getCategoryId()
    //    queryWrapper.eq(Dish::getStatus, 1);//只查启售的菜品，停售(status=0)过滤掉
    //    queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());//先判断了一下category_id不为空
    //    //添加排序条件：按照dish表里的sort字段 升序排列，即sort越小越靠前(orderBy)，再按UpdateTime升序排列，即越晚添加，越靠前
    //    queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
    //
    //    List<Dish> list = dishService.list(queryWrapper);
    //    return R.success(list);//将查询出的list作为data，封装在R对象中
    //}

    /**
     * 根据分类id, 查出该分类的所有菜品(含口味数据)
     * 后期改造：
     * 因为在前台用户端的首页 展示某分类的所有菜品时，如果有口味信息，应该显示"选择规格"，而不是单纯的"+"。所以此处返回值中应该包含口味数据。使用DishDto封装
     *
     * 返回值：查出的是多个菜品dish，所以用List集合盛装。此处返回值中还应该包含口味数据，所以用dishDto代替dish，其中包含flavors属性。
     * 处理请求：/dish/list?categoryId=1397844263642378242
     * 后期改造：
     * 在前台用户端的首页 展示某分类的所有菜品时，如果有口味信息，应该显示"选择规格"，而不是单纯的"+"。所以此处返回值中应该包含口味数据。
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //参数：为什么传来的请求带参数，但是形参用Dish接收？
        //用 (Long categoryId)来接收也可以
        //也可以用实体类Dish dish接收，通用性更强，易于扩展。
        // 只要实体类Dish中的属性名categoryId 和 传输过来的请求参数 name名字("category_id")一致，就可以通过 实体类类型的形参 获取请求参数

        //从表中查询所有的分类：select * from dish where category_id = ? AND status = ?
        //构造条件构造器：
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件：根据category_id查询
        //具体条件 用::的写法 Dish::getCategoryId。具体的值 是传过来的参数dish.getCategoryId()
        queryWrapper.eq(Dish::getStatus, 1);//只查启售的菜品，停售(status=0)过滤掉
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());//先判断了一下category_id不为空
        //添加排序条件：按照dish表里的sort字段 升序排列，即sort越小越靠前(orderBy)，再按UpdateTime升序排列，即越晚添加，越靠前
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        //现在已经从dish表查出了所有菜品信息，还需要从dish_flavor表查出每个菜品对应的口味信息

        List<DishDto> dishDtoList = list.stream().map((item)->{ //item：遍历出的每一个菜品对象dish
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);//把dish的普通属性 copy给 我们自己创建的dishDto对象

            //在dishDto对象中，添加categoryName属性的值
            Long categoryId = item.getCategoryId();//获取当前遍历到的 dish对象的 categoryId
            //根据id，从数据库查询分类对象，以获取分类名称
            Category category = categoryService.getById(categoryId);//根据id查分类对象
            if(category!=null){
                String categoryName = category.getName();//获取分类名称
                dishDto.setCategoryName(categoryName);//把categoryName赋值给 我们自己创建的dishDto对象的 categoryName属性
            }

            //在dishDto对象中，添加flavors属性的值
            Long dishId = item.getId();//获得当前菜品id
            // 获取口味数据
            // select * from dish_flavor where dish_id = ?
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);//返回从dish_flavor表查出的 某个菜品id对应的所有口味数据
            dishDto.setFlavors(flavors);//把flavors赋值给 我们自己创建的dishDto对象的 flavors属性

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);//将查询出的dishDtoList作为data，封装在R对象中
    }
}
