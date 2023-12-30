package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获最底层业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


    /**
     * 捕获SQLIntegrity完整性异常，并返回错误信息
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result SQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        //拿到信息进行拼接
        String message = ex.getMessage();
        log.info(message);
        if (message.contains("Duplicate entry")) {
            String[] s = message.split(" ");
            String msg;
            msg = s[5].contains("dish_name") ?  s[2].concat("菜品名称重复!") : s[2].concat("用户名重复!");
            return Result.error(msg);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
