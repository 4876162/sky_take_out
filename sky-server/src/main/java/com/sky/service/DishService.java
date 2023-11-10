package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.result.Result;

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

}
