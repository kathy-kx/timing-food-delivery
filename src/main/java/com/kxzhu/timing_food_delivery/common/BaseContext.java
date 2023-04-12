package com.kxzhu.timing_food_delivery.common;

/**
 * @ClassName BaseContext
 * @Description 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id.
 *
 * 在"公共字段自动填充"的功能中，createUser和updateUser的用户id是固定值，需要改成【动态获取当前登录用户的id】
 * MyMetaObjectHandler类中，是不能获得HttpSession对象的，所以不能从session中获取当前登录用户的id（"employee"），可以使用ThreadLocal来解决
 *
 * ThreadLocal是什么：可以为每个线程提供一个单独的存储空间，像Map一样保存、取数据。只有在线程内 可以获取到相应的值
 *
 * 为什么可以用：由于每次发送http请求，服务器会分配一个新线程来处理。
 * 而LoginCheckFilter的doFilter()方法、EmployeeController的save()/update()方法、MyMetaObjectHandler的insertFill()/updateFill()方法每次都在同一个线程中，
 * 才能用ThreadLocal存、取值
 *
 * ThreadLocal常用方法：
 * threadLocal.set(id);
 * threadLocal.get();
 *
 * 过程：
 * 每次请求 - 到LoginCheckFilter检查是否放行 - 其中要检查是否登录，若登录了则通过调用BaseContext.get(id)，拿到用户id，放进threadLocal（在同一线程这一作用域内有效）
 * 到元数据对象处理器MyMetaObjectHandler中，统一为公共字段赋值——它实现了MetaObjectHandler，当sql执行insert或update时，会执行MetaObjectHandler的insertFill()和updateFill()方法
 *
 * @Author zhukexin
 * @Date 2023-02-18 15:33
 */
public class BaseContext {

    //属性
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();//ThreadLocal对象实例化定义的时候，一般都是static类型
    //为了存id，所以泛型用Long

    //方法
    public static void setCurrentId(Long id){//工具类 设置成了static
        threadLocal.set(id);
    }
    public static Long getCurrentId(){
        return threadLocal.get();
    }

}
