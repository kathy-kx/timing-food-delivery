package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName EmployeeMapper
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-14 10:32
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> { //BaseMapper是mybatis plus提供的，可以继承增删改查方法。需要提供一个泛型，即实体类
}
