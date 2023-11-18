package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into setmeal(category_id, name, price, status, description," +
            " image, create_time, update_time, create_user, update_user) " +
            "VALUE(#{categoryId},#{name},#{price},#{status},#{description}," +
            "#{image},#{createTime},#{updateTime},#{createUser},#{updateUser}) ")
    void insertSetmeal(Setmeal setmeal);

    Page<SetmealVO> getPage(SetmealPageQueryDTO setmealPageQueryDTO);

    @Update("update setmeal set status = #{status} where id = #{id}")
    void changeStatus(Integer status, Integer id);

    void deleteSetMeal(List<Integer> ids);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getSetMeal(Integer id);

    @Select("select * from setmeal where id = #{id}")
    SetmealVO getById(Integer id);

    void updateSetMeal(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
