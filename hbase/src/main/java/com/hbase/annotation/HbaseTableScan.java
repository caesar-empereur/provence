package com.hbase.annotation;

import java.lang.annotation.*;

import org.springframework.context.annotation.Import;

import com.hbase.core.HbaseTableScanHandler;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HbaseTableScanHandler.class)
public @interface HbaseTableScan {

    String value() default "";
}
