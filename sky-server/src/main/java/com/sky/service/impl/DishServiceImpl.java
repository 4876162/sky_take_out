package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    //同时操作两张表，添加事务
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
        flavors.forEach((dishFlavor) -> {
            dishFlavor.setDishId(dish.getId());
        });

        //批量插入
        dishFlavorMapper.batchInsert(flavors);

        return Result.success("新增成功！");
    }


    @Override
    public PageResult getPage(DishPageQueryDTO dishPageQueryDTO) {

        //获取分页参数
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();

        //开启分页查询
        PageHelper.startPage(page, pageSize);

        //查询分页数据
        Page<DishVO> dishPage = dishMapper.getPage(dishPageQueryDTO);

        return new PageResult(dishPage.getTotal(),dishPage.getResult());
    }
}
