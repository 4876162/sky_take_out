package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: ReportController
 *
 * @Author Mobai
 * @Create 2023/11/25 13:03
 * @Version 1.0
 * Description:
 */

@RestController
@RequestMapping("/admin/report")
public class ReportController {

    @Autowired
    private ReportService reportService;


    /**
     * 获取指定时间段内的营业额
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result getturnoverStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        TurnoverReportVO turnoverReportVO = reportService.getturnoverStatistic(begin, end);

        return Result.success(turnoverReportVO);
    }


    /**
     * 获取用户统计数据
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result getUserStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        UserReportVO userReportVO = reportService.getUserStatistic(begin, end);

        return Result.success(userReportVO);
    }

    /**
     * 获取订单统计数据
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result getOrderStatistic(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        OrderReportVO orderStatistic = reportService.getOrderStatistic(begin, end);

        return Result.success(orderStatistic);
    }

    /**
     * 获取Top10的菜品
     *
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    public Result getTop10(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin
            , @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        SalesTop10ReportVO top10ReportVO = reportService.getTop10(begin, end);

        return Result.success(top10ReportVO);
    }


    /**
     * 导出订单数据统计数据
     * @param httpServletResponse
     */
    @GetMapping("/export")
    public void exportStatistic(HttpServletResponse httpServletResponse) {      //通过HttpServletResponse向客户端发送数据

        reportService.getStatisticExcel(httpServletResponse);

    }

}
