package com.sky.mapper;

import com.sky.dto.OrdersDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ClassName: OrderDetailMapper.xml
 *
 * @Author Mobai
 * @Create 2023/11/19 18:19
 * @Version 1.0
 * Description:
 */

@Mapper
public interface OrderDetailMapper {

    void batchInsert(List<OrderDetail> list);

    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> getList(Orders order);
}
