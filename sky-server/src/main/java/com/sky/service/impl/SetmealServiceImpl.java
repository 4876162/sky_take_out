package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ClassName: SetmealServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/12 15:55
 * @Version 1.0
 * Description:
 */

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public Result InsertSetmeal(SetmealDTO setmealDTO) {

        //新增套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.insertSetmeal(setmeal);

        //获取套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //给list中的SetMealId赋值
        setmealDishes.forEach((item) -> {
            item.setSetmealId(setmeal.getId());
        });

        //插入菜品套餐关系表
        setmealDishMapper.batchInsert(setmealDishes);

        return Result.success("新增成功！");
    }

    @Override
    public Result<PageResult> getPage(SetmealPageQueryDTO setmealPageQueryDTO) {

        //获取分页参数
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        //开启分页查询
        PageHelper.startPage(page, pageSize);

        //查询分页数据
        Page<SetmealVO> setmealPage = setmealMapper.getPage(setmealPageQueryDTO);

        return Result.success(new PageResult(setmealPage.getTotal(), setmealPage.getResult()));
    }

    @Override
    public Result changeStatus(Integer status, Long id) {

        setmealMapper.changeStatus(status, id);

        return Result.success("状态更新成功!");
    }

    @Override
    public Result removeSetMeal(List<Integer> ids) {

        for (Integer id : ids) {
            Setmeal setMeal = setmealMapper.getSetMeal(id);
            if (setMeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException("套餐起售中，无法删除！");
            }
        }

        setmealMapper.deleteSetMeal(ids);

        return Result.success("删除成功!");
    }

    @Override
    public Result getByIdWithSetMealDishes(Integer id) {

        SetmealVO setmealVO = setmealMapper.getById(id);

        List<SetmealDish> setmealDishList = setmealDishMapper.getBySetMealId(id);

        setmealVO.setSetmealDishes(setmealDishList);

        return Result.success(setmealVO);
    }

    @Override
    @Transactional
    public Result modifySetMeal(SetmealDTO setmealDTO) {

        //新建Setmeal对象，属性拷贝
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //更新Setmeal
        setmealMapper.updateSetMeal(setmeal);

        //获取setMealDish
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //删除原有数据
        setmealDishMapper.deleteBySetMealId(setmeal.getId());

        //判断修改后的列表是否有值
        if (setmealDishes != null && setmealDishes.size() > 0) {
            //给关系表中的setmeal赋值
            setmealDishes.forEach((item) -> {
                item.setSetmealId(setmeal.getId());
            });
            //新增
            setmealDishMapper.batchInsert(setmealDishes);
        }

        return Result.success("修改成功!");
    }
}
