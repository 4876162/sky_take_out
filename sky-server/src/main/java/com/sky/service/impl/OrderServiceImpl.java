package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.GetRoutePlanSN;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.GetCoordinateSN;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: OrderServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/19 17:09
 * @Version 1.0
 * Description:
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private GetCoordinateSN getCoordinateSN;

    @Autowired
    private GetRoutePlanSN getRoutePlanSN;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value(value = "${sky.baidu.ak}")
    private String AK;

    @Value(value = "${sky.shop.address}")
    private String shopAddress;

    /**
     * 处理提交的订单数据
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public Result<OrderSubmitVO> submitOrderWithOrderDetails(OrdersSubmitDTO ordersSubmitDTO) {

        //判断当前用户的购物车是否为空，或者用户的地址簿是否为空
        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> cartList = shoppingCartMapper.getCartList(userId);
        if (cartList == null || cartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //1.构造地址信息
        String address = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();

        //判断当前下单地址和店铺地址是否超过5km
        try {
            checkOutOfBoundary(address);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        //构造订单对象
        Orders orders = new Orders();
        //执行对象拷贝
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //给order对象赋值地址信息
        orders.setAddress(address);
        //订单号
        String uuid = UUID.randomUUID().toString();
        orders.setNumber(uuid);
        //下单时间
        orders.setOrderTime(LocalDateTime.now());
        //订单状态
        orders.setStatus(Orders.PENDING_PAYMENT);
        //设置支付状态
        orders.setPayStatus(Orders.UN_PAID);
        //赋值电话
        orders.setPhone(addressBook.getPhone());
        //赋值收货人
        orders.setConsignee(addressBook.getConsignee());
        //赋值用户名和ID
        orders.setUserId(userId);
        orders.setUserName(userMapper.getUserName(userId));

        //插入订单数据
        orderMapper.insertOrder(orders);

        List<OrderDetail> list = new ArrayList<>();

        for (ShoppingCart s : cartList) {
            //构造order_detail对象
            OrderDetail orderDetail = new OrderDetail();
            //进行属性拷贝
            BeanUtils.copyProperties(s, orderDetail, "id");
            //给order_detail对象赋值OrderId
            orderDetail.setOrderId(orders.getId());
            //添加到列表
            list.add(orderDetail);
        }

        //批量插入详细表
        orderDetailMapper.batchInsert(list);

        //清空购物车
        shoppingCartMapper.cleanCart(userId);

        //构造VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

        //构造WebSocket对象
        Map map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content",orders.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        return Result.success(orderSubmitVO);
    }

    /**
     * 判断是否超出配送范围
     *
     * @param address
     * @return
     */
    public void checkOutOfBoundary(String address) throws UnsupportedEncodingException, NoSuchAlgorithmException {


        //获取用户地址经纬度
        Map params = new LinkedHashMap<String, String>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", AK);
        params.put("sn", getCoordinateSN.caculateSn(address));

        String userLocationJson = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3?", params);

        //转成JsonObject
        JSONObject userCoordinate = JSON.parseObject(userLocationJson);
        //获取Json对象中的状态
        String status = userCoordinate.getString("status");
        if (!status.equals("0")) {
            throw new OrderBusinessException("收货地址解析失败");
        }
        //获取JsonObject中的JsonObject
        JSONObject location = userCoordinate.getJSONObject("result").getJSONObject("location");
        //获取经度纬度
        String lng = location.getString("lng");
        String lat = location.getString("lat");

        lng = lng.substring(0, lng.indexOf("."))
                + lng.substring(lng.indexOf("."), lng.indexOf(".") + 7);

        //保留6位小数
        lat = lat.substring(0, lat.indexOf("."))
                + lat.substring(lat.indexOf("."), lat.indexOf(".") + 7);

        String userLatLng = lat + "," + lng;
        log.info("用户经纬度:{}", userLatLng);


        //获取店铺位置经纬度
        Map params2 = new LinkedHashMap<String, String>();
        params2.put("address", shopAddress);
        params2.put("output", "json");
        params2.put("ak", AK);
        params2.put("sn", getCoordinateSN.caculateSn(shopAddress));

        String shopLocationJson = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3?", params2);

        //转成JsonObject
        JSONObject shopCoordinate = JSON.parseObject(shopLocationJson);
        //获取Json对象中的状态
        status = shopCoordinate.getString("status");
        if (!status.equals("0")) {
            throw new OrderBusinessException("店铺地址解析失败");
        }
        //获取JsonObject中的JsonObject
        location = shopCoordinate.getJSONObject("result").getJSONObject("location");
        //获取经度纬度
        lng = location.getString("lng");
        lat = location.getString("lat");

        lng = lng.substring(0, lng.indexOf("."))
                + lng.substring(lng.indexOf("."), lng.indexOf(".") + 7);

        //保留6位小数
        lat = lat.substring(0, lat.indexOf("."))
                + lat.substring(lat.indexOf("."), lat.indexOf(".") + 7);


        String shopLatLng = lat + "," + lng;
        log.info("店铺经纬度:{}", shopLatLng);


        //路线规划
        Map params3 = new LinkedHashMap<String, String>();
        params3.put("origin", userLatLng);
        params3.put("destination", shopLatLng);
        params3.put("ak", AK);
        String currentTimestamp = String.valueOf(System.currentTimeMillis());
        params3.put("timestamp", currentTimestamp);
        params3.put("steps_info","0");
        params3.put("sn", getRoutePlanSN.caculateSn(userLatLng, shopLatLng));

        try {
            getRoutePlanSN.requestGetSN("https://api.map.baidu.com/directionlite/v1/driving?",params3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving?", params3);

        log.info(json);

        JSONObject routeCoordinate = JSON.parseObject(json);
        String s = routeCoordinate.getString("status");
        if (!s.equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = routeCoordinate.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 获取历史订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getHistory(OrdersPageQueryDTO ordersPageQueryDTO) {

        Long userId = BaseContext.getCurrentId();
        Integer status = ordersPageQueryDTO.getStatus();

        //利用DTO对象来接收返回数据
        //开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //查询分页数据
        Page<Orders> pageInfo = orderMapper.getList(userId, status);

        //根据查询的订单信息
        List<Orders> list = pageInfo.getResult();

        List<OrderVO> voList = new ArrayList<>();
        //遍历查询列表详细信息
        list.forEach((order) -> {
            OrderVO orderVO = new OrderVO();
            //查询详细信息
            List<OrderDetail> orderDetails = orderDetailMapper.getList(order);
            //赋值详细信息
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            //插入列表中
            voList.add(orderVO);
        });

        //通过pageInfo构造一个PageResult对象
        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRecords(voList);

        return pageResult;
    }

    /**
     * 获取订单详细信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<OrderVO> getDetail(Long id) {

        //查询指定订单详细信息
        Orders order = orderMapper.getById(id);
        //创建VO对象拷贝属性
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        List<OrderDetail> list = orderDetailMapper.getList(order);
        orderVO.setOrderDetailList(list);

        return Result.success(orderVO);
    }

    /**
     * 取消订单
     *
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {

        //取消订单
        //更改状态，添加取消时间
        Orders order = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);

    }

    @Override
    public void reOrder(Long id) {

        //重新下单
        //重新放回购物车中
        Orders order = new Orders();
        order.setId(id);
        List<OrderDetail> list = orderDetailMapper.getList(order);

        //将orderDetail转换成shoppingCart
        list.forEach((o) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(o, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            //批量插入
            shoppingCartMapper.insertCartItem(shoppingCart);
        });
    }

    /**
     * 获取订单页面
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getOrderPage(OrdersPageQueryDTO ordersPageQueryDTO) {

        //开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        //查询数据
        Page<Orders> page = orderMapper.getPage(ordersPageQueryDTO);
        List<Orders> list = page.getResult();

        List<OrderVO> voList = concatDishName(list);

        //封装数据
        long total = page.getTotal();
        PageResult pageResult = new PageResult(total, voList);

        return pageResult;
    }

    /**
     * 获取每个状态的订单数量
     *
     * @return
     */
    @Override
    public Result getStatistic() {

        //查询待接单
        Integer countToBeConfirmed = orderMapper.getStatisticByStatus(Orders.TO_BE_CONFIRMED);
        //查询带派送
        Integer countConfirmed = orderMapper.getStatisticByStatus(Orders.CONFIRMED);
        //查询派送中
        Integer countDeliveryInProgress = orderMapper.getStatisticByStatus(Orders.DELIVERY_IN_PROGRESS);

        //获取各种状态的订单数量
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(countConfirmed);
        orderStatisticsVO.setToBeConfirmed(countToBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(countDeliveryInProgress);

        return Result.success(orderStatisticsVO);
    }

    //拼接订单中的菜品信息
    public List<OrderVO> concatDishName(List<Orders> list) {

        List<OrderVO> voList = list.stream().map((order) -> {
            //新建Vo对象
            OrderVO orderVO = new OrderVO();
            //属性拷贝
            BeanUtils.copyProperties(order, orderVO);
            //查询详细信息
            List<OrderDetail> orderDetails = orderDetailMapper.getList(order);
            //拼接字符串(StringBuilder性能更好但是线程不安全,StingBuffer性能更差但是线程安全)
            StringBuilder stringBuilder = new StringBuilder();
            orderDetails.forEach((od) -> {
                stringBuilder.append(od.getName() + "*" + od.getNumber() + ";");
            });
            orderVO.setOrderDishes(stringBuilder.toString());

            return orderVO;
        }).collect(Collectors.toList());

        return voList;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {

        orderMapper.confirm(ordersConfirmDTO);

    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public Result reminder(Long id) {

        String number = orderMapper.getById(id).getNumber();

        //构造WebSocket对象
        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content",number);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);


        return Result.success();
    }
}
