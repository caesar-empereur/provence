package com.hbase.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
public @interface RowKey {

    /**
     * 暂时只支持 String, Number, Date 类型
     * @return
     */

    String[] columnList();
}
