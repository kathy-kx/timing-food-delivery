package com.kxzhu.timing_food_delivery.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用类：服务器返回结果（copy过来的）
 * 服务端controller响应客户端页面发来的请求，controller处理完之后给页面返回结果，把结果封装到此R类对象中
 * @param <T>
 */
@Data
public class R<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    //静态方法。登录成功，可以调用R.success(employee)
    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    //静态方法。登录失败，可以调用R.error(msg)
    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    //操作map对象动态数据
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
