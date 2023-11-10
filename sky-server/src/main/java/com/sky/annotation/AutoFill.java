package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: AutoFill
 *
 * @Author Mobai
 * @Create 2023/11/9 22:10
 * @Version 1.0
 * Description:
 */

@Target(ElementType.METHOD) //指定注解作用范围在方法上
@Retention(RetentionPolicy.RUNTIME) //指定注解生效环境为运行时环境
public @interface AutoFill {

    //指定注解的属性
    OperationType value();

}
