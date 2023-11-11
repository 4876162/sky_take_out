package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    Integer insertDish(Dish dish);

    Page<DishVO> getPage(DishPageQueryDTO dishPageQueryDTO);

    //查询在集合中并且状态在售的菜品
    @Select("select count(*) from dish where id = #{id} and status = 1")
    Integer queryStatusIsSell(Long id);

    @Delete("delete from dish where id = #{id}")
    void removeDish(Long id);

    @Update("update dish set status = #{status} where id = #{id}")
    void changeStatus(Integer status, Integer id);
}
