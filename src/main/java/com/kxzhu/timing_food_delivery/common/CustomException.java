package com.kxzhu.timing_food_delivery.common;

/**
 * @ClassName CustomException
 * @Description 自定义异常类
 * 用于在删除菜品/套餐分类时，检查是否已经关联菜品/套餐
 * @Author zhukexin
 * @Date 2023-02-20 22:44
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {//主要目的是 把提示信息传进来
        super(message);
    }
}
