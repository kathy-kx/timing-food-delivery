package com.kxzhu.timing_food_delivery.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工-实体类
 *
 * mybatis plus提供"公共字段自动填充"的功能。
 * 公共字段指的是，很多表里都会有这些字段（例如更新时间等），不只是employee表
 * 在插入或者更新的时候，为指定字段赋予指定的值。可以统一对这些字段进行处理，避免重复代码。
 * 第一步：给实体类中需要自动填充的属性（公共字段）上，加@TableField注解，在参数fill中指定自动填充的策略（即insert时填充or update时填充）。
 * 第二步：编写元数据对象处理器MyMetaObjectHandler，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口
 *
 */
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;    //身份证号。在表中对应的是id_number。
    // 只要在application.yml中开启驼峰命名，就没问题 map-underscore-to-camel-case: true

    private Integer status;

    @TableField(fill = FieldFill.INSERT)//插入的时候，填充字段的值
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)//插入和更新时填充字段的值
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
