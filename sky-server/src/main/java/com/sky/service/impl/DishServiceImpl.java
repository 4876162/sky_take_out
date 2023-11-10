package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: DishServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/10 10:01
 * @Version 1.0
 * Description:
 */

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    //添加事务
    @Transactional
    @Override
    public Result addDish(DishDTO dishDTO) {

        //属性对拷,排除flavors
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish, "flavors");

        //插入菜品
        dishMapper.insertDish(dish);

        //循环设置口味对应的菜品ID
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors = flavors.stream().map((flavor) -> {
            flavor.setDishId(dish.getId());
            return flavor;
        }).collect(Collectors.toList());

        //批量插入
        for (DishFlavor flavor :
                flavors) {
            dishFlavorMapper.addFlavor(flavor);
        }

        return Result.success("新增成功！");
    }

}
