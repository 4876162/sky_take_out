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
import org.springframework.data.redis.core.RedisTemplate;
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

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

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

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        //判断缓存中是否有套餐数据
        String cacheName = "setmeal_" + setmeal.getCategoryId();
        if (redisTemplate.hasKey(cacheName)) {
            //返回缓存数据
            log.info("读取缓存:" + cacheName);
            String json = (String) redisTemplate.opsForValue().get(cacheName);
            List<Setmeal> setmealList = JSON.parseArray(json, Setmeal.class);
            return setmealList;
        }
        //不存在数据
        List<Setmeal> list = setmealMapper.list(setmeal);

        //存入缓存
        redisTemplate.opsForValue().set(cacheName, JSON.toJSONString(list));

        return list;
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {

        //查询是否存在套餐菜品对应缓存
        String cacheName = "setmeal_dish_" + id;
        if (redisTemplate.hasKey(cacheName)) {
            log.info("读取缓存:" + cacheName);
            //返回缓存数据
            String json = (String) redisTemplate.opsForValue().get(cacheName);
            //转成java对象
            List<DishItemVO> dishItemVOList = JSON.parseArray(json, DishItemVO.class);
            return dishItemVOList;
        }

        List<DishItemVO> list = setmealMapper.getDishItemBySetmealId(id);
        //缓存数据
        redisTemplate.opsForValue().set(cacheName, JSON.toJSONString(list));

        return list;
    }
}
