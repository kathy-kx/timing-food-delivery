package com.kxzhu.timing_food_delivery.dto;


import com.kxzhu.timing_food_delivery.entity.Setmeal;
import com.kxzhu.timing_food_delivery.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
