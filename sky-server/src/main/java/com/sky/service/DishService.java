package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * ClassName: DishService
 *
 * @Author Mobai
 * @Create 2023/11/10 10:01
 * @Version 1.0
 * Description:
 */

public interface DishService {

    Result addDish(DishDTO dishDTO);


    PageResult getPage(DishPageQueryDTO dishPageQueryDTO);

    Result batchRemove(List<Long> ids);

    Result changeStatus(Integer status, Long id);

    Result<DishVO> getByDishId(Long id);

    Result modifyDish(DishDTO dishDTO);

    Result getByCategoryId(Long categoryId);
}
