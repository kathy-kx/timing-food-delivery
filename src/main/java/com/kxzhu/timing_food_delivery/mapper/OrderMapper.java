package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName OrderMapper
 * @Description 下单
 * @Author zhukexin
 * @Date 2023-03-15 15:34
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
