package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ClassName: SetmealDishMapper
 *
 * @Author Mobai
 * @Create 2023/11/11 15:42
 * @Version 1.0
 * Description:
 */

@Mapper
public interface SetmealDishMapper {


    @Select("select count(*) from setmeal_dish where dish_id = #{id}")
    Integer queryDependSetmeal(Long id);

    void batchInsert(List<SetmealDish> setmealDishes);

    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetMealId(Integer id);

    @Delete("delete from setmeal_dish where setmeal_id = #{id};")
    void deleteBySetMealId(Long id);
}
