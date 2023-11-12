package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: DishController
 *
 * @Author Mobai
 * @Create 2023/11/10 9:58
 * @Version 1.0
 * Description:
 */

@Api(tags = "菜品管理")
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

    /**
     * 菜品分页查询
     * @return
     */
    @GetMapping("/page")
    public Result queryPage(DishPageQueryDTO dishPageQueryDTO) {

        PageResult page = dishService.getPage(dishPageQueryDTO);

        return Result.success(page);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @ApiOperation(value = "删除菜品")
    @DeleteMapping
    public Result removeDish(@RequestParam List<Long> ids) {

        Result result = dishService.batchRemove(ids);

        return result;
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @ApiOperation(value = "修改菜品状态")
    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status,Long id) {

        Result result = dishService.changeStatus(status, id);

        return result;
    }

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id获取菜品数据")
    @GetMapping("/{id}")
    public Result<DishVO> getDish(@PathVariable Long id) {

        Result result = dishService.getByDishId(id);

        return result;
    }

    /**
     * 修改菜品数据
     * @param dishDTO
     * @return
     */
    @ApiOperation(value = "修改菜品数据")
    @PutMapping
    public Result modifyDish(@RequestBody DishDTO dishDTO) {

        Result result = dishService.modifyDish(dishDTO);

        return result;
    }

    /**
     * 根据分类Id获取菜品列表
     * @param categoryId
     * @return
     */
    @ApiOperation(value = "根据分类获取菜品列表")
    @GetMapping("/list")
    public Result getDishByCategoryId(Long categoryId) {

        Result result = dishService.getByCategoryId(categoryId);

        return result;
    }


}
