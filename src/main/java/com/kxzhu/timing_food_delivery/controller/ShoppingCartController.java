package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.entity.ShoppingCart;
import com.kxzhu.timing_food_delivery.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName ShoppingCartController
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-13 21:07
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * 处理请求/shoppingCart/add，将菜品添加到shopping_cart表中。
     * json数据中有dishFlavor口味、amount金额、dishId菜品id或者setmealId套餐id、name菜名、image图片，都是shopping_cart表的字段，可以由ShoppingCart对象封装
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}", shoppingCart);
        //设置用户id，指定当前的购物车数据是哪个用户的（传过来的json数据中没有user_id）
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前菜品或者套餐是否已经在购物车当中
        // select * from shopping_cart where user_id = ? and dish_id = ?
        // 或者select * from shopping_cart where user_id = ? and setmeal_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if(shoppingCart.getDishId() != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else if(shoppingCart.getSetmealId() != null){
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //从表中查出的一条数据：(有可能不存在这一条数据，没查到，此时需要新增而非更新)
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        // 判断表中是否已有这个user_id和dish_id。如果已有，则给数量加1。如果没有，则新增一条购物车的数据
        if(cartServiceOne == null){
            //没有这条数据，则新增一条购物车的数据。且第一次新增，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);//已经给shoppingCart设置了user_id
            cartServiceOne = shoppingCart;
        }else {//cartServiceOne != null
            //已有这条数据，则给原来的数量加1
            Integer number = cartServiceOne.getNumber();//从表中查出的这一条已有的数据，获取它的number字段值，并加1
            cartServiceOne.setNumber( number + 1);
            shoppingCartService.updateById(cartServiceOne);//将更改后的这一条数据，更新至表中
        }
        return R.success(cartServiceOne);
    }

    /**
     * 购物车商品减少数量
     * POST请求/shoppingCart/sub
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        //log.info("购物车数据：{}", shoppingCart);

        //判断是菜品还是套餐：看传来的是dishId还是setmealId
        //然后需要根据dishId或者setmealId定位到shopping_cart表中的某一条数据
        // (如果是菜品)select * from shopping_cart where user_id = ? and dish_id = ?
        // 或者(如果是套餐)select * from shopping_cart where user_id = ? and setmeal_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());//user_id已经存在域中和BaseContext中，可以取用
        if(shoppingCart.getDishId() != null){
            //传来的是菜品id，需要减少菜品的数量
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }else if(shoppingCart.getSetmealId() != null){
            //传来的是套餐id，需要减少套餐的数量
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //现在已经找到了表中待修改的一条数据(ShoppingCart对象) - one
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        Integer number = one.getNumber();//表中这条数据对应的菜品数量number
        if(number == 1){
            //菜品数量为1，则直接删除这条记录。无论菜品数量number为何值,都需要-1,只有返回number为0的shoppingcart,才能在页面正常显示(前端要求)
            one.setNumber(number - 1);//
            shoppingCartService.remove(queryWrapper);
        }else{
            //菜品数量非1，则给表中原来的number-1。
            one.setNumber(number - 1);
            shoppingCartService.updateById(one);
        }
        return R.success(one);
    }

    /**
     * 查看购物车内商品的集合
     * 处理请求：/shoppingCart/list
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        //查询 当前用户对应的购物车数据
        //select * from shopping_cart where user_id = ?
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);//从shopping_cart表，查出筛选后的数据
        return R.success(list);
    }

    /**
     * 清空购物车
     * 处理请求/shoppingCart/clean
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //在shopping_cart表中筛选出要删的数据，再删除
        //select * from shopping_cart where user_id = ?
        //为什么不用remove from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

}
