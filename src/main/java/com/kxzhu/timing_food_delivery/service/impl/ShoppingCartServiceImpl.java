package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.ShoppingCart;
import com.kxzhu.timing_food_delivery.mapper.ShoppingCartMapper;
import com.kxzhu.timing_food_delivery.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @ClassName ShoppingCartServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-13 21:07
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
