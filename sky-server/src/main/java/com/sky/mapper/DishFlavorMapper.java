package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName: DishFlavorMapper
 *
 * @Author Mobai
 * @Create 2023/11/10 10:05
 * @Version 1.0
 * Description:
 */

@Mapper
public interface DishFlavorMapper {

    void batchInsert(List<DishFlavor> list);

    @Delete("delete from dish_flavor where dish_id = #{id}")
    void removeDishFlavor(Long id);
}
