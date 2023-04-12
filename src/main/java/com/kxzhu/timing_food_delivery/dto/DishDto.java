package com.kxzhu.timing_food_delivery.dto;

import com.kxzhu.timing_food_delivery.entity.Dish;
import com.kxzhu.timing_food_delivery.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * DishDto，封装页面提交的数据
 * DTO：全称为Data Transfer object，即数据传输对象，一般用于展示层（html页面）与服务层（后端）之间的数据传输。
 * 当浏览器传给服务器的参数，与实体类的属性不是一一对应，则接收json类型的请求参数时，不能直接用已有的实体类Dish+@RequestBody注解来接收
 */
@Data
public class DishDto extends Dish {//继承了Dish的所有属性，还另扩展了三个属性

    private List<DishFlavor> flavors = new ArrayList<>();
    //接收页面传来的json数据 "flavors":[{"name":"甜味","value":"[\"无糖\",\"少糖\",\"半糖\",\"多糖\",\"全糖\"]"
    //实体类DishDTO属性flavors对应json数据的flavors
    //flavors是List类型，盛装的是DishFlavor对象，DishFlavor对象有属性name和value

    private String categoryName;

    private Integer copies;
}
