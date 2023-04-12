package com.kxzhu.timing_food_delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kxzhu.timing_food_delivery.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName OrderDetailMapper
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-03-15 16:09
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
