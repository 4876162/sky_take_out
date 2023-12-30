package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;

/**
 * ClassName: ShoppingCartService
 *
 * @Author Mobai
 * @Create 2023/11/18 15:27
 * @Version 1.0
 * Description:
 */
public interface ShoppingCartService {

    Result addToShoppingCart(ShoppingCartDTO shoppingCartDTO);

    Result getList();

    Result subItem(ShoppingCartDTO shoppingCartDTO);

    void clean();
}
