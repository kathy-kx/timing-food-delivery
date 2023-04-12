package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.User;
import com.kxzhu.timing_food_delivery.mapper.UserMapper;
import com.kxzhu.timing_food_delivery.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-10 13:45
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
