package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ShopController
 *
 * @Author Mobai
 * @Create 2023/11/13 21:13
 * @Version 1.0
 * Description:
 */

@CrossOrigin
@RestController(value = "UserShopController")
@RequestMapping("/user/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 店铺营业状态
     *
     * @return
     */
    @ApiOperation(value = "获取店铺营业状态")
    @GetMapping("/status")
    public Result getShopStatus() {

        Integer status = (Integer) redisTemplate.opsForValue().get("status");

        return Result.success(status);

    }
}
