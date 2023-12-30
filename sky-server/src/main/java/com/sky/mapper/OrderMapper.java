package com.sky.mapper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import com.sky.vo.TurnoverReportVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ClassName: OrderMapper
 *
 * @Author Mobai
 * @Create 2023/11/19 18:05
 * @Version 1.0
 * Description:
 */

@Mapper
public interface OrderMapper {

    void insertOrder(Orders orders);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 获取订单列表
     *
     * @return
     */
    Page<Orders> getList(Long userId, Integer status);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<Orders> getPage(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select count(1) from orders where status = #{status}")
    Integer getStatisticByStatus(Integer status);


    @Update("update orders set status = #{status} where id = #{id}")
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 根据订单状态和下单时间查询订单列表
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 查询某一天的营业额
     */
    BigDecimal getTurnOver(LocalDateTime begin, LocalDateTime end,Integer status);


    /**
     * 根据订单日期和状态统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    Integer countOrderByDateAndStatus(LocalDateTime begin, LocalDateTime end,Integer status);

    /**
     * 获取前10销量的数据
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getTop10NameByDate(LocalDateTime begin, LocalDateTime end,Integer status);


    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
