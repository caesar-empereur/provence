package com.hbase.annotation;

import java.lang.annotation.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.hbase.core.HtableScanHandler;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HtableScanHandler.class)
public @interface HbaseTableScan {

    String value() default "";
}
