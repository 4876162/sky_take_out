package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: OrderController
 *
 * @Author Mobai
 * @Create 2023/11/22 11:53
 * @Version 1.0
 * Description:
 */

@Api(tags = "A端-订单管理")
@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 查询订单页面数据
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result getOrdersPage(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageResult pageResult = orderService.getOrderPage(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 统计各个状态的订单数量
     *
     * @return
     */
    @GetMapping("/statistics")
    public Result getAllStatusStatistics() {

        Result result = orderService.getStatistic();

        return result;
    }


    /**
     * 获取订单详细信息
     *
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result getOrderDetail(@PathVariable Long id) {

        Result<OrderVO> result = orderService.getDetail(id);

        return result;
    }


    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {

        ordersConfirmDTO.setStatus(Orders.CONFIRMED);

        orderService.confirm(ordersConfirmDTO);

        return Result.success("接单成功！");
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {

        orderService.reject(ordersRejectionDTO);

        return Result.success("拒单成功！");
    }


    /**
     * 取消订单
     *
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id) {
        orderService.delivery(id);
        return Result.success();
    }


    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }

}
