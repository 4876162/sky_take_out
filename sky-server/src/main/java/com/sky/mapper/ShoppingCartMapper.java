package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassName: ShoppingCartMapper
 *
 * @Author Mobai
 * @Create 2023/11/18 15:31
 * @Version 1.0
 * Description:
 */
@Mapper
public interface ShoppingCartMapper {


    ShoppingCart getCartItem(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "value (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insertCartItem(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number},amount = #{amount} where id = #{id} and user_id = #{userId}")
    void updateCartItem(ShoppingCart cartItem);

    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> getCartList(Long userId);

    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId};")
    void removeItem(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void cleanCart(Long userId);

}
