package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * ClassName: OrderService
 *
 * @Author Mobai
 * @Create 2023/11/19 17:09
 * @Version 1.0
 * Description:
 */

public interface OrderService {

    Result<OrderSubmitVO> submitOrderWithOrderDetails(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult getHistory(OrdersPageQueryDTO ordersPageQueryDTO);

    Result<OrderVO> getDetail(Long id);

    void cancelOrder(Long id);

    void reOrder(Long id);

    PageResult getOrderPage(OrdersPageQueryDTO ordersPageQueryDTO);

    Result getStatistic();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商家取消订单
     *
     * @param ordersCancelDTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 派送订单
     *
     * @param id
     */
    void delivery(Long id);


    /**
     * 完成订单
     *
     * @param id
     */
    void complete(Long id);

    Result reminder(Long id);
}
