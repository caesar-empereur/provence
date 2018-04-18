package com.app.annotation;

import com.app.core.HbaseTableScanHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

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
