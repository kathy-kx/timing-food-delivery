package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.OrderDetail;
import com.kxzhu.timing_food_delivery.mapper.OrderDetailMapper;
import com.kxzhu.timing_food_delivery.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @ClassName OrderDetailServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 16:10
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
