package com.app.config;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HbaseTableScanHandler.Registrar.class)
public @interface HbaseTableScan {

    @AliasFor("basePackages")
    String name() default "";
}
