package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.entity.Category;

/**
 * @ClassName CategoryService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 17:02
 */
public interface CategoryService extends IService<Category> {

    /**
     * 根据id删除分类
     * 之前只是调用mybatis plus提供的功能，直接进行删除，但是没有检查删除的分类是否关联了菜品或者套餐，
     * 所以自己写，进行功能完善
     * @param id
     */
    public void remove(Long id);
}
