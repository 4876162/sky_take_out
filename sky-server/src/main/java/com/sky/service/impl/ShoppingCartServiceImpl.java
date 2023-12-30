package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: ShoppingCartServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/18 15:28
 * @Version 1.0
 * Description:
 */

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public Result addToShoppingCart(ShoppingCartDTO shoppingCartDTO) {
/*

        Long setmealId = shoppingCartDTO.getSetmealId();
        Long dishId = shoppingCartDTO.getDishId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();

        //创建购物车对象
        ShoppingCart shoppingCart = new ShoppingCart();
        //属性拷贝
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //设置用户ID
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询数据库判断当前购物车中菜品是否存在
        ShoppingCart cartItem = shoppingCartMapper.getCartItem(shoppingCart);

        //如果传入的是菜品数据
        if (setmealId == null) {
            //查询菜品数据
            DishVO dishVO = dishMapper.getById(dishId);
            //设置dish_id
            shoppingCart.setDishId(dishId);
            //设置口味数据
            shoppingCart.setDishFlavor(dishFlavor);

            //如果购物车中不存在当前菜品或者口味数据不等
            if (cartItem == null || (shoppingCartDTO.getDishFlavor() != null && !shoppingCartDTO.getDishFlavor().equals(cartItem.getDishFlavor()))) { //新建菜品数据
                //设置初始数量1
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(dishVO.getPrice());
                //属性拷贝
                BeanUtils.copyProperties(dishVO, shoppingCart);
                //设置创建时间
                shoppingCart.setCreateTime(LocalDateTime.now());
                //设置菜品价格
                shoppingCart.setAmount(dishVO.getPrice());
                //新增购物车数据
                shoppingCartMapper.insertCartItem(shoppingCart);
            } else {
                //数量加1
                cartItem.setNumber(cartItem.getNumber() + 1);
                shoppingCartMapper.updateCartItem(cartItem);
            }

        } else {
            //查询套餐数据
            Setmeal setMeal = setmealMapper.getSetMeal(shoppingCartDTO.getSetmealId());

            shoppingCart.setSetmealId(setmealId);

            //如果购物车中不存在当前菜品
            if (cartItem == null) {
                //设置初始数量1
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(setMeal.getPrice());
                //属性拷贝
                BeanUtils.copyProperties(setMeal, shoppingCart, "createTime");
                //设置创建时间
                shoppingCart.setCreateTime(LocalDateTime.now());
                //设置套餐价格
                shoppingCart.setAmount(setMeal.getPrice());
                //新增购物车数据
                shoppingCartMapper.insertCartItem(shoppingCart);
            } else {
                //数量加1
                cartItem.setNumber(cartItem.getNumber() + 1);
                shoppingCartMapper.updateCartItem(cartItem);
            }
        }
*/


        /**
         *结构二
         */
        ShoppingCart shoppingCart = new ShoppingCart();
        //拷贝属性
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        //设置用户名
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //设置数量
        shoppingCart.setNumber(1);
        //设置创建时间
        shoppingCart.setCreateTime(LocalDateTime.now());
        //查询是否已经存在数据
        ShoppingCart cartItem = shoppingCartMapper.getCartItem(shoppingCart);

        //如果添加的Item已经存在表中，就让Number+1
        if (cartItem != null) {
            cartItem.setNumber(cartItem.getNumber() + 1);
            //更新CartItem
            shoppingCartMapper.updateCartItem(cartItem);
        } else {
            //当添加的Item不存在的时候
            //如果是菜品
            if (shoppingCartDTO.getDishId() != null) {
                //查询菜品
                DishVO dishVO = dishMapper.getById(shoppingCartDTO.getDishId());
                //构造菜品数据
                BeanUtils.copyProperties(dishVO, shoppingCart);
                //设置价格
                shoppingCart.setAmount(dishVO.getPrice());
            } else {
                //查询套餐数据
                Setmeal setMeal = setmealMapper.getSetMeal(shoppingCartDTO.getSetmealId());
                //构造套餐数据
                BeanUtils.copyProperties(setMeal, shoppingCart);
                //设置价格
                shoppingCart.setAmount(setMeal.getPrice());
            }
            //插入数据到购物车表
            shoppingCartMapper.insertCartItem(shoppingCart);
        }

        return Result.success("添加成功!");
    }


    @Override
    public Result getList() {

        ///获取当前用户的购物车
        Long currentId = BaseContext.getCurrentId();

        List<ShoppingCart> cartList = shoppingCartMapper.getCartList(currentId);

        return Result.success(cartList);
    }


    @Override
    public Result subItem(ShoppingCartDTO shoppingCartDTO) {

        //构造ShoppingCart
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        ShoppingCart cartItem = shoppingCartMapper.getCartItem(shoppingCart);

        //如果购物车中的Item数量大于1，就让number减少1
        if (cartItem.getNumber() > 1) {
            cartItem.setNumber(cartItem.getNumber() - 1);
            shoppingCartMapper.updateCartItem(cartItem);
        } else {
            //数量<=1移除Item
            shoppingCartMapper.removeItem(cartItem);
        }

        return Result.success("移除成功!");
    }

    @Override
    public void clean() {

        //获取用户ID
        Long currentId = BaseContext.getCurrentId();
        //清除购物车
        shoppingCartMapper.cleanCart(currentId);
    }
}
