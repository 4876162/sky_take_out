package com.sky.service;

import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * ClassName: ReportService
 *
 * @Author Mobai
 * @Create 2023/11/25 13:08
 * @Version 1.0
 * Description:
 */
public interface ReportService {

    TurnoverReportVO getturnoverStatistic(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistic(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistic(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end);

    void getStatisticExcel(HttpServletResponse httpServletResponse);
}
