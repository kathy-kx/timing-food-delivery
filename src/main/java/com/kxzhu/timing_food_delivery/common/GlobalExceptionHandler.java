package com.kxzhu.timing_food_delivery.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @ClassName GlobalExceptionHandler
 * @Description 全局异常处理类，捕获处理各种可能出现的异常。把处理异常的方法封装到此类中，简化代码
 * 通过aop，拦截到save等控制器方法，在此处理
 * 并返回错误信息给前端
 * @Author zhukexin
 * @Date 2023-02-15 22:39
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})//指定拦截哪些controller
//加了@RestController注解或@Controller注解的方法，会被该拦截器 拦截下来处理
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     * 一旦标注了@RestController注解或@Controller注解的控制器方法抛SQLIntegrityConstraintViolationException异常，
     * 就会被这个exceptionHandler()方法拦截到，在这里处理
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class) //处理哪个异常的方法
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());//异常信息：SQLIntegrityConstraintViolationException：Duplicate entry 'zhangsan' for key 'employee.idx_username'
        if(ex.getMessage().contains("Duplicate entry")){    //说明违反了数据表的UNIQUE约束
            String[] split = ex.getMessage().split(" ");//将错误信息按空格划分开，index=2时是username信息
            String msg = split[2] + "已存在"; //xxx（username）已存在
            return R.error(msg);//给前端页面返回
        }
        return R.error("未知错误");//给前端页面返回
    }

    /**
     * 异常处理方法
     * 一旦标注了@RestController注解或@Controller注解的控制器方法抛CustomException异常,
     * 就会被这个exceptionHandler()方法拦截到，在这里处理
     * 返回错误信息给前端
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class) //处理哪个异常的方法
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());//异常信息：CustomException：已关联菜品/套餐，不能删除
        return R.error(ex.getMessage());//给前端页面返回
    }
}
