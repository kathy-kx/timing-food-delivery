package com.kxzhu.timing_food_delivery.dto;

import com.kxzhu.timing_food_delivery.entity.OrderDetail;
import com.kxzhu.timing_food_delivery.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    //自己添加的：
    private Integer sumNum;//商品数量，前端order.html需要该数据

    private List<OrderDetail> orderDetails;
	
}
