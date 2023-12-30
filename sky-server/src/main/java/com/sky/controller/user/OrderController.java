package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: OrderController
 *
 * @Author Mobai
 * @Create 2023/11/19 17:06
 * @Version 1.0
 * Description:
 */

@Slf4j
@Api(tags = "C端-订单模块")
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {

        Result<OrderSubmitVO> result = orderService.submitOrderWithOrderDetails(ordersSubmitDTO);

        return result;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }


    /**
     * 获取历史订单数据
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {

        //这里使用OrderDTO来传输对象数据
        PageResult pageResult = orderService.getHistory(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 获取订单详细信息
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {

        Result<OrderVO> result = orderService.getDetail(id);

        return result;
    }


    /**
     * 取消订单
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {

        orderService.cancelOrder(id);

        return Result.success();
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result reOrder(@PathVariable Long id) {

        orderService.reOrder(id);

        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    public Result reminderOrder(@PathVariable Long id) {

        Result result = orderService.reminder(id);

        return result;
    }

}
