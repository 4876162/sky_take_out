package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ShoppingCartController
 *
 * @Author Mobai
 * @Create 2023/11/18 15:21
 * @Version 1.0
 * Description:
 */

@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result addItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {

        Result result = shoppingCartService.addToShoppingCart(shoppingCartDTO);

        return result;
    }

    /**
     * 获取购物车的内容
     *
     * @return
     */
    @GetMapping("/list")
    public Result getCartItem() {

        Result result = shoppingCartService.getList();

        return result;
    }

    /**
     * 减少菜品数量
     *
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    public Result removeCartItem(@RequestBody ShoppingCartDTO shoppingCartDTO) {

        Result result = shoppingCartService.subItem(shoppingCartDTO);

        return result;
    }


    @DeleteMapping("/clean")
    public Result cleanCart() {

        shoppingCartService.clean();

        return Result.success("清除成功！");
    }

}
