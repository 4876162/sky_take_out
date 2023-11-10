package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

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


    @Insert("insert into dish_flavor(dish_id, name, value) values (#{dishId},#{name},#{value})")
    void addFlavor(DishFlavor dishFlavor);

}
