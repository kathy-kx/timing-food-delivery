package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.entity.Employee;

/**
 * @ClassName EmployeeService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-14 10:34
 */
public interface EmployeeService extends IService<Employee> { // IService是mybatis plus提供的，需要提供一个泛型，即实体类
}
