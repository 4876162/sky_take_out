package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.ResolverUtil;
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

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置店铺状态
     *
     * @param status
     * @return
     */
    @ApiOperation(value = "设置店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {

        log.info("设置店铺营业状态为:{}", status == 1 ? "营业中" : "打烊中");

        redisTemplate.opsForValue().set("status", status);

        return Result.success();
    }

    /**
     * 店铺营业状态
     *
     * @return
     */
    @ApiOperation(value = "获取店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {

        Integer status = (Integer) redisTemplate.opsForValue().get("status");

        return Result.success(status);

    }
}
