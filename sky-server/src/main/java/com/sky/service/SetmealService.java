package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

/**
 * ClassName: SetmealService
 *
 * @Author Mobai
 * @Create 2023/11/12 15:55
 * @Version 1.0
 * Description:
 */


public interface SetmealService {

    Result InsertSetmeal(SetmealDTO setmealDTO);

    Result<PageResult> getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    Result changeStatus(Integer status, Long id);

    Result removeSetMeal(List<Integer> ids);

    Result getByIdWithSetMealDishes(Integer id);

    Result modifySetMeal(SetmealDTO setmealDTO);
}
