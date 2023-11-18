package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: SetmealController
 *
 * @Author Mobai
 * @Create 2023/11/12 15:28
 * @Version 1.0
 * Description:
 */

@Api(tags = "套餐管理")
@CrossOrigin
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;


    /**
     * 新增套餐
     *
     * @param setmealDTO
     * @return
     */
    @ApiOperation(value = "新增套餐")
    @PostMapping
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {

        Result result = setmealService.InsertSetmeal(setmealDTO);

        return result;
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @ApiOperation(value = "套餐分页查询")
    @GetMapping("/page")
    public Result<PageResult> getPage(SetmealPageQueryDTO setmealPageQueryDTO) {

        Result<PageResult> result = setmealService.getPage(setmealPageQueryDTO);

        return result;
    }

    /**
     * 修改套餐状态
     * @param status
     * @param id
     * @return
     */
    @ApiOperation(value = "套餐状态更改")
    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status,Integer id) {

        Result result = setmealService.changeStatus(status, id);

        return result;
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @ApiOperation(value = "删除套餐")
    @DeleteMapping
    public Result remove(@RequestParam List<Integer> ids) {

        Result result = setmealService.removeSetMeal(ids);

        return result;
    }

    /**
     * 根据套餐id查询套餐
     * @param id
     * @return
     */
    @ApiOperation(value = "根据套餐id查询套餐")
    @GetMapping("/{id}")
    public Result getSetMealById(@PathVariable Integer id) {

        Result result = setmealService.getByIdWithSetMealDishes(id);

        return result;
    }

    /**
     * 修改套餐
     * @return
     */
    @ApiOperation(value = "修改套餐")
    @PutMapping
    public Result modifySetMeal(@RequestBody SetmealDTO setmealDTO) {

        Result result = setmealService.modifySetMeal(setmealDTO);

        return result;
    }

}
