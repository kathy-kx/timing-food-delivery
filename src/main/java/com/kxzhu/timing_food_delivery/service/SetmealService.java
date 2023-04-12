package com.kxzhu.timing_food_delivery.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kxzhu.timing_food_delivery.dto.SetmealDto;
import com.kxzhu.timing_food_delivery.entity.Setmeal;

/**
 * @ClassName SetmealService
 * @Description TODO
 * @Author zhukexin
 * @Date 2023-02-20 22:15
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时保存与菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 根据id，查询套餐（含套餐内的菜品）
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 修改套餐信息，以及套餐包含什么菜品
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
