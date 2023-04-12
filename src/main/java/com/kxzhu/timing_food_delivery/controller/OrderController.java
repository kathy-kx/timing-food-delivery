package com.kxzhu.timing_food_delivery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.R;
import com.kxzhu.timing_food_delivery.dto.DishDto;
import com.kxzhu.timing_food_delivery.dto.OrdersDto;
import com.kxzhu.timing_food_delivery.entity.*;
import com.kxzhu.timing_food_delivery.service.OrderDetailService;
import com.kxzhu.timing_food_delivery.service.OrderService;
import com.kxzhu.timing_food_delivery.service.ShoppingCartService;
import com.kxzhu.timing_food_delivery.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrderController
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 15:37
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired OrderDetailService orderDetailService;

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    UserService userService;

    /**
     * 下单
     * 请求/order/submit，method=post
     * 携带的参数是备注信息remark、付款方式payMethod、addressBookId，都在Orders类中
     * 用户id是不需要传来的。因为用户登录后，可以从session获得，或者从BaseContext(ThreadLocal)中获得
     * 下单菜品不需要传。因为下单了什么菜品/套餐，可以根据用户id，从购物车表里查到
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("支付成功");
    }

    /**
     * 点“查看订单”，会显示订单详情
     * 请求/order/userPage?page=1&pageSize=5
     * 使用OrdersDto，它除了Orders类的属性之外，额外扩展了userName、phone、address、consignee、orderDetails属性。并给OrdersDto添加属性private int sumNum;
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        /*
    返回类型：R，泛型是Page。因为需要传给浏览器records、total数据，以显示分页页面
    参数：请求中带着参数(http://localhost:8080/order/userPage?page=1&pageSize=5)发过来，声明和请求参数同名的形参
    跳页码时，请求参数带着page和pageSize
    */

        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();//参考return R.success(pageInfo)。方法结束时，需要响应一个Page类型的分页构造器对象。
        // 且该对象中含有"orderDetails"商品详情，所以还需要构造一个OrdersDto实体对应的Page对象

        // 查询 sql：select * from orders where user_id = ?
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行分页查询（用orderService，查orders表）
        orderService.page(pageInfo, queryWrapper); //处理好又会封装到Page类型的pageInfo中，pageInfo中就有值了，含有数据列表records、总记录数total等信息

        // 前端需要：orderTime, status, orderDetails(含name, number), sumNum, amount
        // 现在已经查出了orderTime, status, amount(表中是单价，前端会处理后再展示吗？)
        // 还差orderDetails(含name, number), sumNum，所以我们需要将它俩封装到OrdersDto中，再传回给前端return R.success(ordersDtoPage)

        // (1) 将已经从orders表中查出的分页信息pageInfo的各属性值 复制给ordersDtoPage（除records）
        BeanUtils.copyProperties(pageInfo, ordersDtoPage,"records");//不拷贝records属性（页面上的数据信息就是records，需要处理）
        // (2) 再处理records：把records转换为OrdersDto类型的：添加上orderDetails(含name, number), sumNum，设置给ordersDtoPage
        List<Orders> records = pageInfo.getRecords();//把pageInfo里的records部分拿出来。
// 需要处理该List集合：原本是List集合盛装Orders，即List<Orders>类型的records，不含订单详情和总商品数；需要变成List集合盛装OrdersDto，即List<OrdersDto>类型的records，含订单详情和总商品数。
        // 然后设置ordersDtoPage的records为新的集合list：ordersDtoPage.setRecords(list);
        List<OrdersDto> list = records.stream().map((item)->{ //item：遍历出的每一个菜品对象orders
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);//把orders的普通属性 copy给 我们自己创建的ordersDto对象
            // ordersDto现在还差orderDetails(含name, number), sumNum
            Long orderId = item.getId();//获取当前遍历到的 orders对象的 id，凭此订单id，去order_details表中查找该订单对应的订单详情
            // select * from order_details where order_id = ?
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapper);//将查询出的*(即某订单id对应的所有商品详情)封装为一个List<OrderDetail>集合
            ordersDto.setOrderDetails(orderDetails); //给当前ordersDto对象填上orderDetails属性值

            //ordersDto现在还差sumNum商品数量，需要去ordersDetail中获取每一商品项的数量，并累加
            Integer sumNum = 0;//初始化
            for (OrderDetail orderDetail : orderDetails) {
                Integer number = orderDetail.getNumber();
                sumNum += number;
            }
            ordersDto.setSumNum(sumNum);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);//把加了categoryName的新list 赋值给dishDtoPage的records属性

        return R.success(ordersDtoPage);
    }


    /**
     * 再来一单
     * 处理逻辑一：(有问题)
     * 需要先清空该用户的购物车，然后根据订单id，从order_detail表查出该订单的具体菜品（集合），并加入购物车。再通过购物车结算。
     *
     * 问题：如果购物车内已经加了别的商品，直接通过结算购物车内所有商品的方式则不对。
     * 因为跳转到add-order.html页面会立即获取购物车内的商品数据和默认地址，带着地址和购物车数据下单，执行controller中的submit()方法。
     * 而我们希望的是：在订单/个人页面点"再来一单"，带着订单id想要跳转到add-order.html页面，还是会立即获取购物车内的商品数据和默认地址，这样不对。
     * 简化解决办法：先清空该用户的购物车。这样因为加上事务，也有问题，没能清空，全下单了
     * @param orders
     * @return
     */
    //@Transactional //多表操作（shopping_cart, orders, order_detail）//不能使用事务，否则会把购物车里的和这次添加的全部下单（而且是下两单）
    @PostMapping("/again2")
    public R<String> again2(@RequestBody Orders orders){
        //先清空该用户的购物车：
        //在shopping_cart表中筛选出要删的数据，再删除 select * from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> clearQueryWrapper = new LambdaQueryWrapper<>();
        clearQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(clearQueryWrapper);

        //获取订单id
        Long orderId = orders.getId();

        //从order_detail表查出该订单的具体菜品/套餐（集合）
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //将菜品/套餐（集合）加入购物车。即，将菜品添加到shopping_cart表中。
        Long userId = BaseContext.getCurrentId();//shopping_cart表需要，但是orderDetail中没有
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map( (item) -> { // item是一个个OrderDetail对象
            ShoppingCart shoppingCart = new ShoppingCart();

            BeanUtils.copyProperties(item, shoppingCart, "id", "orderId");
            //shoppingCart还缺id(主键)、user_id、createTime
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setId(IdWorker.getId());

            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);//将这一条条shoppingCart数据保存到shopping_cart表

        return R.success("再下一单成功");
    }


    /**
     * 再来一单。响应回前端后，直接跳转到支付成功页面的写法
     * 处理ajax请求/order/again，method=post，携带参数是"再来一单"对应订单的订单id(order的id)。
     *
     * 参数：可以是Integer id。但是也可以是Orders orders（含有订单id这一属性，且和浏览器传过来的属性同名），更具通用性
     * 思路：根据"再来一单"处的订单号，在数据库中找到与该订单，下一个新订单与该订单内容完全一样（除了id、时间、状态）
     * 向orders表和order_details表插入数据
     * @param orders
     * @return
     */
    @Transactional // 多表操作
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        //1、向orders表插入1条数据：
        //根据订单id，查找orders表，获取该订单数据对应的对象(select * from orders where id = ? )
        Long originalOrderId = orders.getId();
        Orders originalOrder = orderService.getById(originalOrderId);

        //重新生成id、number、status(2)、orderTime、checkOutTime，并设置给新订单。其余的直接从原来订单copy
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(originalOrder, newOrder, "id","number","status","orderTime", "checkoutTime");
        long newOrderId = IdWorker.getId();
        newOrder.setId(newOrderId);
        newOrder.setNumber(String.valueOf(newOrderId));
        newOrder.setStatus(2);
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setCheckoutTime(LocalDateTime.now());
        orderService.save(newOrder);

        //2、向order_detail表插入多条数据：
        // 根据传来的订单id，找到order_detail表中对应的多条数据，获取集合对象
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, originalOrderId);
        List<OrderDetail> originalOrderDetails = orderDetailService.list(queryWrapper);

        // 新订单的newOrderDetails集合可以复制原来的orderDetails集合,但是order_id、和主键id需要重新设置
        //List<OrderDetail> newOrderDetails = new ArrayList<>();
        //BeanUtils.copyProperties(originalOrderDetails, newOrderDetails, "id", "orderId");

        List<OrderDetail> newOrderDetails = originalOrderDetails.stream().map( (item) ->{ // item为一个个OrderDetail对象（原订单的）
            OrderDetail newOrderDetail = new OrderDetail();
            BeanUtils.copyProperties(item, newOrderDetail, "id", "orderId");
            newOrderDetail.setId(IdWorker.getId());
            newOrderDetail.setOrderId(newOrderId);

            return newOrderDetail;
        }).collect(Collectors.toList());// 转化为新订单的一个个OrderDetail对象

        orderDetailService.saveBatch(newOrderDetails);
        return R.success("再来一单成功");
    }

    /**
     * 后台管理页面-查看"订单明细"分页数据
     * 通过url带着page、pageSize、number(订单号)、beginTime、endTime等参数，发送请求到/order/page，method=get。需要服务器处理，并响应给前端分页数据。
     * 返回类型：R，泛型是Page。因为需要传给浏览器records、total数据，以显示分页页面
     * 参数：请求中带着参数(http://localhost:8080/order/page?page=1&pageSize=10&number=xxxx&beginTime=xxx&endTime=xxx)发过来，声明和请求参数同名的形参
     * 其中number、beginTime、endTime是在搜索框搜索时才会有
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String number, String beginTime, String endTime){

        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        // 页面需要的信息包含订单号number、订单status对应的中文字符串、用户userName、手机号phone、地址address、下单时间orderTime、实收金额amount
        // 这些都在orders表中，所以用Page<Orders>封装即可，前端都可以展示，不需要再使用OrdersDto。

        // select * from orders like '%number值%' and order_time in (beginTime, endTime)
        // 只要number不为空，就根据number字段模糊查询。只要beginTime, endTime不为空，就根据beginTime, endTime字段查询
        //包装查询对象queryWrapper
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件（模糊查询）：只要number不为空，就在orders表中，根据number字段模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(number), Orders::getNumber, number);//like方法的参数：执行条件，字段，值。相当于：LIKE '%值%'
        // 添加查询条件（区间）：只要beginTime和endTime不为空，就根据beginTime, endTime字段查询
        if (beginTime != null && endTime != null) { //beginTime和endTime要么都传来，要么都为null
            queryWrapper.ge(Orders::getOrderTime, beginTime);
            queryWrapper.le(Orders::getOrderTime, endTime);
        }
        // 添加排序条件：按照Orders表里的下单时间 倒序排列，即新下单的排在前面(orderBy)
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行分页查询（用orderService，查orders表）
        orderService.page(pageInfo, queryWrapper); //处理好又会封装到Page类型的pageInfo中，pageInfo中就有值了，含有数据列表records、总记录数total等信息

        return R.success(pageInfo);
    }



    /**
     * 后台管理页面-修改订单状态
     * 处理请求/order，method=put。携带数据是id(订单id)和status(订单状态)。
     * 需要服务端处理该请求，根据订单id，修改orders表中的status，并响应回前端。
     * @param orders
     * @return
     */
    @PutMapping
    public R<Orders> editStatus(@RequestBody Orders orders){
        Long orderId = orders.getId();//获取订单编号

        //根据订单编号，在orders表中查到这一条数据，并修改订单状态status
        Orders ordersInDB = orderService.getById(orderId);//该id对应的 数据库中的订单记录
        ordersInDB.setStatus(orders.getStatus());
        orderService.updateById(ordersInDB);

        return R.success(ordersInDB);
    }

}
