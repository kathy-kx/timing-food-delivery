package com.kxzhu.timing_food_delivery.controller;

import com.kxzhu.timing_food_delivery.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName OrderDetailController
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 16:11
 */
@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;


}
