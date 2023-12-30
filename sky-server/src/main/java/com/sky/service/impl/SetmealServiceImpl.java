package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ClassName: SetmealServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/12 15:55
 * @Version 1.0
 * Description:
 */

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.categoryId")
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
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result changeStatus(Integer status, Integer id) {

        setmealMapper.changeStatus(status, id);

        return Result.success("状态更新成功!");
    }

    @Override
    @CacheEvict(cacheNames = "setmeal_dish", allEntries = true)
    public Result removeSetMeal(List<Long> ids) {

        for (Long id : ids) {
            Setmeal setMeal = setmealMapper.getSetMeal(id);
            if (setMeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException("套餐起售中，无法删除!");
            }
        }

        // 这里无需批量移除redis中套餐缓存数据，因为菜品被禁用状态才能删除
        // 当被禁用的时候，页面是不显示菜品数据的
        int success = setmealMapper.deleteSetMeal(ids);

        if(success >= 1) {
            return Result.success("删除成功!");
        } else {
            return Result.error("删除失败!");
        }
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
    @CacheEvict(cacheNames = {"setmeal", "setmeal_dish"}, allEntries = true)
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

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    @Cacheable(cacheNames = "setmeal", key = "#setmeal.categoryId")
    public List<Setmeal> list(Setmeal setmeal) {
        //不存在数据
        List<Setmeal> list = setmealMapper.list(setmeal);

        return list;
    }

    /**
     * 根据id查询套餐中菜品选项
     *
     * @param id
     * @return
     */
    @Cacheable(cacheNames = "setmeal_dish", key = "#id")
    public List<DishItemVO> getDishItemById(Long id) {

        List<DishItemVO> list = setmealMapper.getDishItemBySetmealId(id);

        return list;
    }
}
