package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: MyTask
 *
 * @Author Mobai
 * @Create 2023/11/21 11:44
 * @Version 1.0
 * Description:
 */

@Component
@Slf4j
public class MyTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 取消支付超时的订单
     * 间隔: 1min
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void cancelTimeout() {

        LocalDateTime now = LocalDateTime.now();

        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, now.minusMinutes(15));

        //设置列表中订单状态
        if (list != null && list.size() > 0) {
            list.forEach((order) -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单支付超时!");
                orderMapper.update(order);
            });
        }
    }

    /**
     * 取消商家前一天未点击完成(派送中)的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cancelDeliveringOrders() {

        LocalDateTime now = LocalDateTime.now();

        List<Orders> list = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, now.minusHours(1));

        //循环遍历设置订单状态和取消原因
        if (list != null && list.size() > 0) {
            list.forEach((order) -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }
    }
}
