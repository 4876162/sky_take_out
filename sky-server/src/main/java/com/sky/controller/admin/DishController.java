package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: DishController
 *
 * @Author Mobai
 * @Create 2023/11/10 9:58
 * @Version 1.0
 * Description:
 */

@CrossOrigin
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品新增
     * @return
     */
    @ApiOperation(value = "新增菜品")
    @PostMapping
    public Result addDish(@RequestBody DishDTO dishDTO) {

        Result result = dishService.addDish(dishDTO);

        return result;
    }


}
