package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ReportServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/25 13:08
 * @Version 1.0
 * Description:
 */

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获得营业额统计数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getturnoverStatistic(LocalDate begin, LocalDate end) {

        //拼接日期
        //1.判断传入日期是否合法
        if (begin == null || end == null || begin.isAfter(end)) {
            throw new RuntimeException("起始结束日期不合法");
        }

        List<LocalDate> dateList = getDateList(begin, end);

        List<BigDecimal> turnoverList = new ArrayList<>();
        //通过group by查询每天的营业额
        for (LocalDate localDate : dateList) {
            BigDecimal number = orderMapper.getTurnOver(LocalDateTime.of(localDate, LocalTime.MIN),
                    LocalDateTime.of(localDate, LocalTime.MAX), Orders.COMPLETED);
            //判断当天营业额是否为null
            number = (number == null) ? BigDecimal.ZERO : number;

            turnoverList.add(number);
        }

        TurnoverReportVO reportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .turnoverList(StringUtils.join(turnoverList, ','))
                .build();

        return reportVO;
    }

    /**
     * 根据起始和结束日期获取时间列表
     *
     * @param begin
     * @param end
     * @return
     */
    public List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //用一个List存储日期，方便后续查询每日营业额
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.isEqual(end)) {
            //让日期往后增加一天
            begin = begin.plusDays(1);
            //将日期添加到list中
            dateList.add(begin);
        }

        return dateList;
    }

    @Override
    public UserReportVO getUserStatistic(LocalDate begin, LocalDate end) {

        //1.判断传入日期是否合法
        if (begin == null || end == null || begin.isAfter(end)) {
            throw new RuntimeException("起始结束日期不合法");
        }

        //获取时间列表
        List<LocalDate> dateList = getDateList(begin, end);
        List<Integer> totalList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        //获取用户总量
        for (LocalDate date : dateList) {

            Integer totalUser = userMapper.getTotalUserLt(LocalDateTime.of(date, LocalTime.MAX));

            Integer newUser = userMapper.getNewUser(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

            totalUser = totalUser == null ? 0 : totalUser;
            newUser = newUser == null ? 0 : newUser;

            totalList.add(totalUser);
            newUserList.add(newUser);
        }

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .totalUserList(StringUtils.join(totalList, ','))
                .newUserList(StringUtils.join(newUserList, ','))
                .build();

        return userReportVO;
    }


    /**
     * 获取订单统计数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistic(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = getDateList(begin, end);


        List<Integer> orderDayTotalCountList = new ArrayList<>();
        List<Integer> orderValidCountList = new ArrayList<>();
        //遍历获取每日的订单数
        dateList.forEach((date) -> {
            //获取每日订单数
            Integer totalDay = orderMapper.countOrderByDateAndStatus(LocalDateTime.of(date, LocalTime.MIN)
                    , LocalDateTime.of(date, LocalTime.MAX), null);
            totalDay = totalDay == null ? 0 : totalDay;
            orderDayTotalCountList.add(totalDay);

            //获取每日有效订单数
            Integer totalValid = orderMapper.countOrderByDateAndStatus(LocalDateTime.of(date, LocalTime.MIN)
                    , LocalDateTime.of(date, LocalTime.MAX), Orders.COMPLETED);
            totalValid = totalValid == null ? 0 : totalValid;
            orderValidCountList.add(totalValid);
        });

        //获取时间段内订单总数
        Integer total = orderDayTotalCountList.stream().reduce(Integer::sum).get();
        total = total == null ? 0 : total;

        //获取时间段内总有效订单数
        Integer totalValid = orderValidCountList.stream().reduce(Integer::sum).get();
        totalValid = totalValid == null ? 0 : totalValid;


        //计算订单完成率
        Double completeRate = (total == 0) ? 0.0 : totalValid.doubleValue() / total;

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(orderDayTotalCountList, ','))
                .validOrderCountList(StringUtils.join(orderValidCountList, ','))
                .totalOrderCount(total)
                .validOrderCount(totalValid)
                .orderCompletionRate(completeRate)
                .build();

        return orderReportVO;
    }


    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.
                getTop10NameByDate(LocalDateTime.of(begin, LocalTime.MIN),
                        LocalDateTime.of(end, LocalTime.MAX), Orders.COMPLETED);

        //通过Stream流封装nameList数据
        List<String> nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String name = StringUtils.join(nameList, ",");

        List<Integer> numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String number = StringUtils.join(numberList, ",");

        //返回封装对象
        return SalesTop10ReportVO.builder()
                .nameList(name)
                .numberList(number)
                .build();
    }


    /**
     * 获取30天内的订单数据
     *
     * @param httpServletResponse
     * @return
     */
    @Override
    public void getStatisticExcel(HttpServletResponse httpServletResponse) {


        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        try {
            FileInputStream fileInputStream = new FileInputStream("classpath:template\\运营数据报表模板.xlsx");
            //获取报表模板
            XSSFWorkbook excel = new XSSFWorkbook(fileInputStream);
            XSSFSheet sheet = excel.getSheetAt(0);

            BigDecimal totalTurnover = BigDecimal.ZERO;
            BigDecimal averageTotalPrice;
            Double totalCompleteRate;
            Integer totalOrder = 0;
            Integer totalNewUser = 0;
            Integer totalValidOrder = 0;

            int row = 0;
            while (!begin.isEqual(end)) {
                LocalDateTime dayBegin = LocalDateTime.of(begin, LocalTime.MIN);
                LocalDateTime dayEnd = LocalDateTime.of(begin, LocalTime.MAX);

                //获取营业额数据
                BigDecimal turnOver = orderMapper.getTurnOver(dayBegin, dayEnd, Orders.COMPLETED);
                turnOver = turnOver == null ? BigDecimal.ZERO : turnOver;

                //获取有效订单
                Integer completed = orderMapper.countOrderByDateAndStatus(dayBegin, dayEnd, Orders.COMPLETED);
                completed = completed == null ? 0 : completed;
                Integer all = orderMapper.countOrderByDateAndStatus(dayBegin, dayEnd, null);
                all = all == null ? 0 : all;

                //获取订单完成率
                Double completeRate;
                if (all == null || all == 0) {
                    completeRate = 0.0;
                } else {
                    completeRate = completed.doubleValue() / all;
                }

                //获取新增用户数
                Integer newUser = userMapper.getNewUser(dayBegin, dayEnd);
                newUser = newUser == null ? 0 : newUser;

                //获取平均客单价
                BigDecimal averagePrice;
                if (turnOver == null || turnOver == BigDecimal.ZERO) {
                    averagePrice = BigDecimal.ZERO;
                } else {
                    averagePrice = turnOver.divide(BigDecimal.valueOf(completed), RoundingMode.DOWN);
                }

                //插入数据表中
                XSSFRow sheetRow = sheet.getRow(7 + row);
                //设置数据
                sheetRow.getCell(1).setCellValue(begin.toString());
                sheetRow.getCell(2).setCellValue(turnOver.doubleValue());
                sheetRow.getCell(3).setCellValue(completed);
                sheetRow.getCell(4).setCellValue(completeRate);
                sheetRow.getCell(5).setCellValue(averagePrice.doubleValue());
                sheetRow.getCell(6).setCellValue(newUser);

                //累加到总数
                totalTurnover = totalTurnover.add(turnOver);
                totalOrder += all;
                totalValidOrder += completed;
                totalNewUser += newUser;

                begin = begin.plusDays(1);
                row++;
            }

            if (totalOrder == 0) {
                totalCompleteRate = 0.0;
            } else {
                totalCompleteRate = totalValidOrder.doubleValue() / totalOrder;
            }

            if (totalValidOrder == 0) {
                averageTotalPrice = BigDecimal.ZERO;
            } else {
                averageTotalPrice = totalTurnover.divide(BigDecimal.valueOf(totalValidOrder), RoundingMode.DOWN);
            }

            //写入概览数据
            XSSFRow row1 = sheet.getRow(3);
            XSSFRow row2 = sheet.getRow(4);
            row1.getCell(2).setCellValue(totalTurnover.doubleValue());
            row1.getCell(4).setCellValue(totalCompleteRate);
            row1.getCell(6).setCellValue(totalNewUser);

            row2.getCell(2).setCellValue(totalValidOrder);
            row2.getCell(4).setCellValue(averageTotalPrice.doubleValue());

            //写回数据流
            excel.write(httpServletResponse.getOutputStream());

            //关闭流
            excel.close();
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
