package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.entity.Orders;

/**
 * @ClassName OrderService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 15:36
 */
public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * 在service中扩展功能
     * @param orders
     */
    public void submit(Orders orders);
}
