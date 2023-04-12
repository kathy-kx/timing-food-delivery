package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.entity.User;

/**
 * @ClassName UserService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-10 13:45
 */
public interface UserService extends IService<User> {
    // IService是mybatis plus提供的，需要提供一个泛型，即实体类
}
