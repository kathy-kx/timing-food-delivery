package com.kxzhu.timing_food_delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kxzhu.timing_food_delivery.entity.Employee;
import com.kxzhu.timing_food_delivery.mapper.EmployeeMapper;
import com.kxzhu.timing_food_delivery.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @ClassName EmployeeServiceImpl
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-14 10:36
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{
}
