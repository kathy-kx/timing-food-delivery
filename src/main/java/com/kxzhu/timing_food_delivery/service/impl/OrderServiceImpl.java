package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.common.BaseContext;
import com.kxzhu.timing_food_delivery.common.CustomException;
import com.kxzhu.timing_food_delivery.entity.*;
import com.kxzhu.timing_food_delivery.mapper.OrderMapper;
import com.kxzhu.timing_food_delivery.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 15:36
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * 在service中扩展功能
     * @param orders
     */
    @Override
    @Transactional //多表操作
    public void submit(Orders orders) {
        //获得当前用户的id
        Long currentId = BaseContext.getCurrentId();

        //查询该用户的购物车数据(可能有很多条数据)
        // select * from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper); //购物车所有商品的集合

        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new CustomException("购物车为空，不能下单");//为了安全进行校验
        }

        //向orders表插入数据之前，先准备好要插入的数据：
        // (1)从user表查该user_id的用户信息(可以填充user_name)；
        User user = userService.getById(currentId);//currentId就是user表中的主键，所以可以用此方法。如果不是主键，只能构造queryWrapper来查

        // (2)从address表查该用户的地址信息(可以填充address, consignee, phone)
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook == null){
            throw new CustomException("用户地址有误，不能下单");//为了安全进行校验。后面要用查出的addressBook对象
        }
        // (3)填数据
        long orderId = IdWorker.getId();//用mybatis-plus的IdWorker生成订单号，类型Long
        AtomicInteger amount = new AtomicInteger(0);//订单总金额初始化（不是最终金额，还需要获取订单详情，再计算总金额）
        //原子操作，线程安全。保证多线程时也没问题。但是只有整型Integer、Long和Boolean型。所以此处金额只能是整数，否则损失精度

        //想把ShoppingCart对象组成的集合shoppingCartList，变为OrderDetail对象组成的集合。
        // OrderDetail对象需要的id, name, image, dish_id, setmeal_id, number, amount, dish_flavor是ShoppingCart对象已经有的。但是amount需要修改
        List<OrderDetail> orderDetails = shoppingCartList.stream().map( (item) -> {// item是该登录用户的一个个ShoppingCart对象
             OrderDetail orderDetail = new OrderDetail();
             BeanUtils.copyProperties(item, orderDetail, "userId", "createTime");//这几个属性是ShoppingCart对象独有，OrderDetail对象没有，需要ignore。
             // 还差什么属性：order_id
             orderDetail.setOrderId(orderId);
             // amount需要修改为总金额（原来表中是单价，前端帮我们计算了总金额。此处需要后端自己再计算总金额）
             amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());// amount += (单价*数量)
             // amount是BigDecimal类型，需要将multiply()的参数也变为BigDecimal类型，才能用这个方法。也就是：大数.multiply(大数)，得到一个大数
            // 通过.intValue()转化为Integer，才能使用addAndGet方法（AtomicInteger类提供的方法）
             return orderDetail;
         }).collect(Collectors.toList());

        //准备数据：
        orders.setNumber(String.valueOf(orderId));//订单号，类型String
        orders.setId(orderId);//订单表的主键，也用订单号
        orders.setStatus(2);//订单状态2，待派送
        orders.setUserId(currentId);
        //addressBookId 已经由json请求传过来，已在orders里
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        //payMethod 已经由json请求传过来，已在orders里
        orders.setAmount(new BigDecimal(amount.get()));//计算总金额
        //remark 已经由json请求传过来，已在orders里
        orders.setPhone(addressBook.getPhone());
        String address = (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail());
        orders.setAddress(address);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());

        //向orders表插入数据。只有一个订单，插入一条数据
        this.save(orders);

        //向order_detail表插入数据。一个订单里有多个菜品，插入多条数据，共享同一个order_id
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);

    }
}
