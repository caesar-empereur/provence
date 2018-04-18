package com.app.annotation;

import com.app.reflection.HbaseTableScanHandler;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HbaseTableScanHandler.class)
public @interface HbaseTableScan {

    @AliasFor("basePackages")
    String value() default "";
}
